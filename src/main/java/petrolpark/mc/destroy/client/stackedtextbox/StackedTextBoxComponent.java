package petrolpark.mc.destroy.client.stackedtextbox;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import petrolpark.mc.destroy.config.DestroyAllConfigs;

/**
 * One segment of a {@link StackedTextBox} — plain text, a chemical-name (bracketed), or an
 * interactive definition (braces). Used by {@link AbstractStackedTextBox#getTextAndActivationAreas}
 * to break a raw string into renderable + hoverable components.
*/
public abstract class StackedTextBoxComponent {

    public final String value;

    public StackedTextBoxComponent(String value) {
        this.value = value;
    }

    /** Break this component's text into the smallest sets of characters that can be line-wrapped together.*/
    public abstract String[] getWords();

    public static class Plain extends StackedTextBoxComponent {

        private List<String> words;

        public Plain(String value) {
            super(value);
            setWords(value);
        }

        protected void setWords(String value) {
            words = new LinkedList<>();
            BreakIterator iterator = BreakIterator.getLineInstance(Minecraft.getInstance().getLocale());
            iterator.setText(value);
            int start = iterator.first();
            for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                String word = value.substring(start, end);
                words.add(word);
            }
        }

        @Override
        public String[] getWords() {
            return words.toArray(new String[words.size()]);
        }
    }

    public static class Molecule extends Plain {

        public Molecule(String value) {
            super(value);
            String[] nameSpaceAndId = value.split(":");
            String key = nameSpaceAndId[0] + ".chemical." + nameSpaceAndId[1];
            String iupacKey = key + ".iupac";
            if (DestroyAllConfigs.CLIENT.chemistry.iupacNames.get() && I18n.exists(iupacKey)) key = iupacKey;
            String moleculeName = Component.translatable(key).getString();
            setWords(moleculeName);
        }
    }

    public static class Definition extends StackedTextBoxComponent {

        private String displayedString;
        public final String definitionTranslationKey;

        public Definition(String value) {
            super(value);
            String[] s = value.split(",");
            if (s.length != 2) {
                displayedString = Component.translatable("destroy.chemistry.unknown_definition").getString();
                definitionTranslationKey = "destroy.chemistry.unknown_definition";
            } else {
                displayedString = "_" + s[0] + "_";
                String[] definitionId = s[1].trim().split(":");
                definitionTranslationKey = definitionId[0] + ".chemistry." + definitionId[1];
            }
        }

        @Override
        public String[] getWords() {
            return new String[] { displayedString };
        }
    }
}
