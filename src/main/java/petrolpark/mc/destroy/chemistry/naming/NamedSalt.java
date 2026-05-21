package petrolpark.mc.destroy.chemistry.naming;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.client.DestroyLang;

/**
 * A {@link INameableProduct} that names a salt (cation + anion pair) for client-side mixture display.
 * Consults {@link SaltNameOverrides} for override translation keys like "sodium chloride" instead of
 * "sodium ion and chloride ion".
*/
@OnlyIn(Dist.CLIENT)
public class NamedSalt implements INameableProduct {

    public final LegacySpecies cation;
    public final LegacySpecies anion;

    public NamedSalt(LegacySpecies cation, LegacySpecies anion) {
        this.cation = cation;
        this.anion = anion;
    }

    @Override
    public Component getName(boolean iupac) {

        SaltNameOverrides cationOverrides = SaltNameOverrides.ALL_OVERRIDES.get(cation);
        SaltNameOverrides anionOverrides = SaltNameOverrides.ALL_OVERRIDES.get(anion);

        Component cationName;
        Component anionName;

        if (cationOverrides != null) {
            String cationKey = cation.getTranslationKey(iupac);

            // Check for an override of the whole salt name
            String withAnionKey = cationOverrides.specificOverrideKeys.get(anion);
            if (withAnionKey != null) {
                if (iupac) withAnionKey += ".iupac";
                if (I18n.exists(withAnionKey)) return Component.translatable(withAnionKey);
            }

            // Check for an override if the cation is part of any salt
            String genericOverrideKey = cationOverrides.genericOverrideKey;
            if (genericOverrideKey != null) {
                if (iupac) genericOverrideKey += ".iupac";
                if (I18n.exists(genericOverrideKey)) cationKey = genericOverrideKey;
            }
            // Set the cation name
            cationName = Component.translatable(cationKey);
        } else {
            // Set the cation name normally
            cationName = cation.getName(iupac);
        }

        if (anionOverrides != null) {
            String anionKey = cation.getTranslationKey(iupac);

            // Check for an override of the whole salt name
            String withCationKey = anionOverrides.specificOverrideKeys.get(cation);
            if (withCationKey != null) {
                if (iupac) withCationKey += ".iupac";
                if (I18n.exists(withCationKey)) return Component.translatable(withCationKey);
            }

            // Check for an override if the anion is part of any salt
            String genericOverrideKey = anionOverrides.genericOverrideKey;
            if (genericOverrideKey != null) {
                if (iupac) genericOverrideKey += ".iupac";
                if (I18n.exists(genericOverrideKey)) anionKey = genericOverrideKey;
            }

            // Set the anion name
            anionName = Component.translatable(anionKey);
        } else {
            anionName = anion.getName(iupac);
        }

        return DestroyLang.translate("mixture.simple_salt", cationName, anionName).component();
    }
}
