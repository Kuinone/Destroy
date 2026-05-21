package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import petrolpark.mc.destroy.client.DestroyIcon;
import petrolpark.mc.destroy.core.pollution.PollutionType;

public class DestroyPollutionTypes {

    public static final RegistryEntry<PollutionType<Level>, PollutionType<Level>>

    GREENHOUSE = REGISTRATE.levelPollutionType("greenhouse", DestroyIcon.GREENHOUSE, PollutionType::new),
    OZONE_DEPLETION = REGISTRATE.levelPollutionType("ozone_depletion", DestroyIcon.OZONE_DEPLETION, PollutionType::new),
    ACID_RAIN = REGISTRATE.levelPollutionType("acid_rain", DestroyIcon.ACID_RAIN, PollutionType::new);

    public static final RegistryEntry<PollutionType<ChunkAccess>, PollutionType<ChunkAccess>>

    SMOG = REGISTRATE.chunkPollutionType("smog", DestroyIcon.SMOG, PollutionType::new),
    RADIOACTIVITY = REGISTRATE.chunkPollutionType("radioactivity", DestroyIcon.RADIOACTIVITY, PollutionType::new);
  
    public static final void register() {};
};
