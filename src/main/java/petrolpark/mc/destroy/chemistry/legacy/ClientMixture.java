package petrolpark.mc.destroy.chemistry.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.createmod.catnip.theme.Color;
import net.minecraft.network.chat.Component;

import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.naming.INameableProduct;
import petrolpark.mc.destroy.chemistry.naming.NamedSalt;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

/**
 * A client-side variant of {@link ReadOnlyMixture} that overrides {@code updateColor()} / {@code updateName()}
 * with richer logic — ARGB weighted averaging across Molecule contributions, and salt/solvent/impurity-aware
 * name generation that consults {@link NamedSalt} and config.
*/
public class ClientMixture extends ReadOnlyMixture {

    public void updateColor() {
        float totalColorContribution = 0f;
        float totalRed = 0;
        float totalGreen = 0;
        float totalBlue = 0;
        int totalAlpha = 64;
        for (Entry<LegacySpecies, Float> entry : contents.entrySet()) {
            Color color = new Color(entry.getKey().getColor());
            float colorContribution = entry.getValue() * color.getAlphaAsFloat();
            totalColorContribution += colorContribution;
            totalRed += color.getRed() * colorContribution;
            totalGreen += color.getGreen() * colorContribution;
            totalBlue += color.getBlue() * colorContribution;
            totalAlpha = Math.max(totalAlpha, color.getAlpha());
        }
        color = new Color((int) (totalRed / totalColorContribution), (int) (totalGreen / totalColorContribution),
            (int) (totalBlue / totalColorContribution), totalAlpha).getRGB();
    }

    protected void updateName() {

        try {

            if (translationKey != null && !translationKey.isEmpty()) {
                name = Component.translatable(translationKey);
                return;
            }

            boolean iupac = DestroyAllConfigs.CLIENT.chemistry.iupacNames.get();

            if (contents.size() == 1) {
                name = contents.entrySet().iterator().next().getKey().getName(iupac);
                return;
            }

            boolean neutral = LegacyMixture.areVeryClose(
                getConcentrationOf(DestroyMolecules.PROTON),
                getConcentrationOf(DestroyMolecules.HYDROXIDE));

            List<INameableProduct> products = new ArrayList<>();
            List<LegacySpecies> cations = new ArrayList<>();
            List<LegacySpecies> anions = new ArrayList<>();
            List<LegacySpecies> solvents = new ArrayList<>();
            List<LegacySpecies> impurities = new ArrayList<>();
            boolean thereAreNeutralMolecules = false;
            for (Entry<LegacySpecies, Float> entry : contents.entrySet()) {
                LegacySpecies molecule = entry.getKey();
                if (neutral && (molecule == DestroyMolecules.HYDROXIDE || molecule == DestroyMolecules.PROTON)) continue;
                if (entry.getValue() < IMPURITY_THRESHOLD) {
                    impurities.add(molecule);
                } else if (molecule.hasTag(DestroyMolecules.Tags.SOLVENT)) {
                    solvents.add(molecule);
                } else {
                    if (molecule.getCharge() > 0) {
                        cations.add(molecule);
                    } else if (molecule.getCharge() < 0) {
                        anions.add(molecule);
                    } else {
                        products.add(molecule);
                    }
                }
                if (molecule.getCharge() == 0) thereAreNeutralMolecules = true;
            }

            // Check for salts
            if (cations.size() != 0 || anions.size() != 0) {
                if (cations.size() == 1 && anions.size() == 1) {
                    if (cations.get(0) != DestroyMolecules.PROTON || anions.get(0) != DestroyMolecules.HYDROXIDE)
                        products.add(new NamedSalt(cations.get(0), anions.get(0)));
                } else {
                    products.add(b -> DestroyLang.translate("mixture.salts").component());
                }
            }

            boolean thereAreSolvents = solvents.size() != 0;
            boolean thereAreImpurities = impurities.size() != 0;

            // If two products have the same name, ignore one
            if (products.size() == 2
                && products.get(0).getName(iupac).getString().equals(products.get(1).getName(iupac).getString()))
                products.remove(1);

            if (products.size() == 0) {
                if (solvents.size() == 1) {
                    name = solvents.get(0).getName(iupac).plainCopy();
                } else if (solvents.size() == 2) {
                    name = DestroyLang.translate("mixture.and",
                        solvents.get(0).getName(iupac).getString(),
                        solvents.get(1).getName(iupac).getString()).component();
                } else if (thereAreSolvents) {
                    name = DestroyLang.translate("mixture.solvents").component();
                }
                if (thereAreImpurities && name != null)
                    name = DestroyLang.translate("mixture.dirty", name.getString()).component();

            } else if (products.size() <= 2) {
                if (products.size() == 1) {
                    name = products.get(0).getName(iupac).plainCopy();
                } else {
                    name = DestroyLang.translate("mixture.and",
                        products.get(0).getName(iupac).getString(),
                        products.get(1).getName(iupac).getString()).component();
                }
                if (thereAreSolvents) name = DestroyLang.translate("mixture.solution", name.getString()).component();
                if (thereAreImpurities) name = DestroyLang.translate("mixture.impure", name.getString()).component();
            }
            // else: many products — fall through to default "mixture.mixture"

            if (name == null) name = DestroyLang.translate("mixture.mixture").component();

            if (!thereAreNeutralMolecules && name != null)
                name = DestroyLang.translate("mixture.supersaturated", name.getString()).component();

        } catch (Throwable e) {
            name = DestroyLang.translate("mixture.mixture").component();
        }
    }
}
