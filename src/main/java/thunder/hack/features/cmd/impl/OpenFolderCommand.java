/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.util.Util
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

public class OpenFolderCommand
extends Command {
    public OpenFolderCommand() {
        super("openfolder", "folder");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/"));
            return 1;
        });
    }
}

