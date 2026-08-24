/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.core.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import vazkii.psi.common.Psi;

public class ConfigHandler {

    public static Configuration config;

    public static void synchronizeConfiguration(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // Add config options here

        } catch (Exception e) {
            Psi.logger.error("Failed to load configuration file!", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
