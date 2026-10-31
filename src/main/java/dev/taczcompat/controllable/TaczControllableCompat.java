package dev.taczcompat.controllable;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Client-only entry point. Every class that touches Controllable or TACZ lives behind
 * {@link ClientBootstrap}, so a missing dependency can never cause a class-load crash here.
 */
@Mod(TaczControllableCompat.MOD_ID)
public final class TaczControllableCompat {
    public static final String MOD_ID = "tacz_controllable_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaczControllableCompat() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CompatConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CompatConfig::onConfigLoading);
        ConfigScreenRegistrar.register();

        ModList mods = ModList.get();
        if (!mods.isLoaded("tacz") || !mods.isLoaded("controllable")) {
            LOGGER.warn("[{}] TACZ or Controllable is not installed; compat layer disabled.", MOD_ID);
            return;
        }
        try {
            ClientBootstrap.init();
        } catch (Throwable t) { // LinkageError / NoSuchMethodError if a dependency version differs
            LOGGER.error("[{}] Incompatible TACZ/Controllable version; compat layer disabled.", MOD_ID, t);
        }
    }
}
