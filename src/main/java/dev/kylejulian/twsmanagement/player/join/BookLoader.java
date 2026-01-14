package dev.kylejulian.twsmanagement.player.join;

import dev.kylejulian.twsmanagement.extensions.TextProcessor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public final class BookLoader {

    private BookLoader() {}

    public static ItemStack load(File file, Player player) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            if (meta == null) return null;

            // Title & author
            String title = reader.readLine();
            String author = reader.readLine();

            if (title != null) {
                meta.title(
                        TextProcessor.parse(title, player, player)
                );
            }

            if (author != null) {
                meta.author(
                        TextProcessor.parse(author, player, player)
                );
            }

            List<Component> pages = new ArrayList<>();
            Component currentPage = Component.empty();

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.equalsIgnoreCase("/newpage") || line.equalsIgnoreCase("/np")) {
                    pages.add(currentPage);
                    currentPage = Component.empty();
                    continue;
                }

                currentPage = currentPage
                        .append(
                                TextProcessor.parse(line, player, player)
                        )
                        .append(Component.newline());
            }

            if (!currentPage.equals(Component.empty())) {
                pages.add(currentPage);
            }

            meta.addPages(pages.toArray(Component[]::new));
            book.setItemMeta(meta);
            return book;

        } catch (Exception ignored) {
            return null;
        }
    }
}
