package petrolpark.mc.destroy.client.stackedtextbox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.widget.ElementWidget;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.client.Minecraft;

/**
 * Abstract hover-text stack — interactive tooltip with nested child text boxes triggered by
 * hovering definition-words. Used by {@link petrolpark.mc.destroy.compat.jei.category.HoverableTextCategory
 * HoverableTextCategory} to show reaction-description tooltips with clickable chemistry-term
 * definitions.
*/
public abstract class AbstractStackedTextBox extends ElementWidget {

    protected Minecraft minecraft;

    protected AbstractStackedTextBox parent;
    protected AbstractStackedTextBox child;

    protected AbstractStackedTextBox(int x, int y, AbstractStackedTextBox parent, AbstractStackedTextBox child) {
        super(x, y);
        this.parent = parent;
        this.child = child;
    }

    public abstract void close();

    public static AbstractStackedTextBox NOTHING = new AbstractStackedTextBox(0, 0, null, null) {

        @Override
        public void close() {}

        @Override
        public boolean isActive() {
            return false;
        }
    };

    public static LinesAndActivationAreas getTextAndActivationAreas(String text, int startX, int startY, int maxWidthPerLine,
                                                                     Screen screen, Font font, FontHelper.Palette palette,
                                                                     boolean isTextBox) {

        if (screen == null) return new LinesAndActivationAreas(List.of(), List.of(), startX, startY, 0, 0);

        // Break the String into sections of plain text, Molecule names, and hoverable definitions
        List<StackedTextBoxComponent> components = new ArrayList<>();
        String currentSection = "";
        boolean inMoleculeName = false;
        boolean inDefinition = false;
        boolean failed = false;
        addAllSections: for (char character : text.toCharArray()) {
            switch (character) {
                case '[': {
                    if (inDefinition) {
                        failed = true;
                        break addAllSections;
                    }
                    inMoleculeName = true;
                    components.add(new StackedTextBoxComponent.Plain(String.valueOf(currentSection)));
                    currentSection = "";
                    break;
                }
                case ']': {
                    if (!inMoleculeName) {
                        failed = true;
                        break addAllSections;
                    }
                    inMoleculeName = false;
                    components.add(new StackedTextBoxComponent.Molecule(String.valueOf(currentSection)));
                    currentSection = "";
                    break;
                }
                case '{': {
                    if (inMoleculeName) {
                        failed = true;
                        break addAllSections;
                    }
                    inDefinition = true;
                    components.add(new StackedTextBoxComponent.Plain(String.valueOf(currentSection)));
                    currentSection = "";
                    break;
                }
                case '}': {
                    if (!inDefinition) {
                        failed = true;
                        break addAllSections;
                    }
                    inDefinition = false;
                    components.add(new StackedTextBoxComponent.Definition(String.valueOf(currentSection)));
                    currentSection = "";
                    break;
                }
                default: {
                    currentSection += character;
                }
            }
        }

        if (!currentSection.isEmpty()) {
            components.add(new StackedTextBoxComponent.Plain(String.valueOf(currentSection)));
        }

        if (failed) {
            components = List.of(new StackedTextBoxComponent.Plain("Badly formatted definition: " + text));
        }

        List<Pair<Area, String>> areasAndTextBoxes = new ArrayList<>();
        Area currentActivationArea = new Area(0, 0, 0, 0);

        int maxLineWidth = 0;

        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int currentLineWidth = 0;
        for (StackedTextBoxComponent component : components) {
            addAllWordsInComponent: for (String word : component.getWords()) {
                int wordWidth = font.width(word.replaceAll("_", ""));
                if (currentLineWidth + wordWidth > maxWidthPerLine) {
                    if (currentLineWidth > 0) {
                        String line = currentLine.toString();
                        lines.add(line);
                        currentLine = new StringBuilder();
                        currentLineWidth = 0;
                    } else {
                        lines.add(word);
                        maxLineWidth = Math.max(maxLineWidth, wordWidth);
                        currentActivationArea.minX = startX;
                        currentActivationArea.maxX = startX + wordWidth;
                        continue addAllWordsInComponent;
                    }
                }
                currentLine.append(word);

                currentActivationArea.minX = startX + currentLineWidth;
                currentActivationArea.minY = startY + (isTextBox ? -3 : 0) + (lines.size()) * font.lineHeight;
                currentActivationArea.maxY = startY + (isTextBox ? -3 : 0) + (lines.size() + 1) * font.lineHeight;

                currentLineWidth += wordWidth;
                maxLineWidth = Math.max(maxLineWidth, currentLineWidth);

                currentActivationArea.maxX = startX + currentLineWidth;
            }

            if (component instanceof StackedTextBoxComponent.Definition definition) {
                areasAndTextBoxes.add(Pair.of(currentActivationArea, Component.translatable(definition.definitionTranslationKey).getString()));
            }

            currentActivationArea = new Area(0, 0, 0, 0);
        }
        if (currentLineWidth > 0) {
            lines.add(currentLine.toString());
        }

        Couple<Style> styles = Couple.create(palette.highlight(), palette.primary());
        MutableComponent lineStart = Component.literal("");
        lineStart.withStyle(palette.primary());
        boolean currentlyHighlighted = false;
        List<Component> formattedLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            MutableComponent currentComponent = lineStart.plainCopy();
            String[] split = line.split("_");
            for (String part : split) {
                currentComponent.append(Component.literal(part).withStyle(styles.get(currentlyHighlighted)));
                currentlyHighlighted = !currentlyHighlighted;
            }
            formattedLines.add(currentComponent);
            currentlyHighlighted = !currentlyHighlighted;
        }

        int width = maxLineWidth + 5;
        int height = 11 + (lines.size() + 1) * font.lineHeight;
        // gate flip on isTextBox=true. Paragraphs (isTextBox=false) must NOT flip —
        // they are anchored at fixed layout positions and a flip would mis-position their
        // hyperlink areas. For tooltip text-boxes, the flip+clamp is now done **at render time**
        // in {@link StackedTextBox#doRender} using PoseStack matrix to compute screen-absolute
        // coordinates (instead of the buggy {@code screen.width / 2} layout-local-vs-screen-abs
        // mismatch). So this static getTextAndActivationAreas no longer needs flip logic for
        // tooltip case either — the box's nominal startX/startY is preserved, and screen-edge
        // clamping is applied later when the absolute screen position is known.
        return new LinesAndActivationAreas(formattedLines, areasAndTextBoxes, startX, startY, width, height);
    }

    public static class Area {
        public int minX;
        public int maxX;
        public int minY;
        public int maxY;

        public Area(int x, int y, int width, int height) {
            minX = x;
            minY = y;
            maxX = x + width;
            maxY = y + height;
        }

        public boolean isIn(int x, int y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }
    }

    public static record LinesAndActivationAreas(List<Component> lines, List<Pair<Area, String>> areas,
                                                  int startX, int startY, int width, int height) {}
}
