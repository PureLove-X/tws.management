package dev.kylejulian.twsmanagement.data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kylejulian.twsmanagement.data.entities.MojangUserModel;
import dev.kylejulian.twsmanagement.data.entities.MojangUserNameModel;
import dev.kylejulian.twsmanagement.util.LogUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MojangApi {

	private final Map<String, UUID> cachedIds;

	public MojangApi() {
		this.cachedIds = new HashMap<>();
	}

	@NotNull
    public CompletableFuture<UUID> getPlayerId(@NotNull String name) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (this.cachedIds.containsKey(name)) {
					LogUtils.debug("[MojangApi] Cache hit");
					return this.cachedIds.get(name);
				}

				LogUtils.debug("[MojangApi] Cache miss");

				String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
				URL obj = URI.create(url).toURL();
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");

				ObjectMapper mapper = new ObjectMapper();
				MojangUserModel user = mapper.readValue(con.getInputStream(), MojangUserModel.class);

				UUID userId = user.getId();
				this.cachedIds.put(name, userId);

				return userId;
			}
			catch (IOException e) {
				LogUtils.warn("Unable to find Player "+ name + " from the Mojang Api");
			}

			return null;
		});
	}

	public CompletableFuture<@Nullable Component> getPlayerName(@NotNull UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				if (this.cachedIds.containsValue(playerId)) {
					for (Map.Entry<String, UUID> entry : this.cachedIds.entrySet()) {
						if (entry.getValue().equals(playerId)) {
							LogUtils.debug("[MojangApi] Cache hit");
							return Component.text(entry.getKey());
						}
					}
				}

				LogUtils.debug("[MojangApi] Cache miss");

				String playerIdString = playerId.toString();

				String url = "https://api.mojang.com/user/profiles/" +
						playerIdString.replace("-", "") + "/names";
				URL obj = URI.create(url).toURL();
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				ObjectMapper mapper = new ObjectMapper();
				MojangUserNameModel[] userNames = mapper.readValue(con.getInputStream(), MojangUserNameModel[].class);

				int numberOfNames = userNames.length;
				MojangUserNameModel lastNameChanged = userNames[numberOfNames - 1];

				String name = lastNameChanged.getName();
				this.cachedIds.put(name, playerId);

				return Component.text(name);
			}
			catch (IOException e) {
				LogUtils.warn("Unable to find PlayerId " + playerId + " from the Mojang Api");
				LogUtils.warn(e.getMessage());
			}
			catch (Exception e) {
				LogUtils.error("Unable to find PlayerId " + playerId + " from the Mojang Api");
				LogUtils.error(e.getMessage());
			}

			return null;
		});
	}
}
