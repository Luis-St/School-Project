package net.luis.game.type;

import com.google.common.collect.Lists;
import net.luis.client.games.ludo.LudoClientGame;
import net.luis.client.games.ttt.TTTClientGame;
import net.luis.client.games.wins4.Wins4ClientGame;
import net.luis.client.screen.game.LudoScreen;
import net.luis.client.screen.game.TTTScreen;
import net.luis.client.screen.game.Wins4Screen;
import net.luis.game.Game;
import net.luis.game.GameFactory;
import net.luis.server.games.ludo.LudoServerGame;
import net.luis.server.games.ttt.TTTServerGame;
import net.luis.server.games.wins4.Wins4ServerGame;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * @author Luis-st
 *
 */

public class GameTypes {
	
	private static final List<GameType<?, ?>> GAME_TYPES = Lists.newArrayList();
	
	public static final GameType<LudoServerGame, LudoClientGame> LUDO = register(new GameType<>("Ludo", 2, 4, new GameFactory<>(LudoServerGame::new, LudoClientGame::new), LudoScreen::new));
	public static final GameType<TTTServerGame, TTTClientGame> TIC_TAC_TOE = register(new GameType<>("Tic Tac Toe", 2, 2, new GameFactory<>(TTTServerGame::new, TTTClientGame::new), TTTScreen::new));
	public static final GameType<Wins4ServerGame, Wins4ClientGame> WINS_4 = register(new GameType<>("4 Wins", 2, 2, new GameFactory<>(Wins4ServerGame::new, Wins4ClientGame::new), Wins4Screen::new));
	
	private static <S extends Game, C extends Game> GameType<S, C> register(GameType<S, C> gameType) {
		GAME_TYPES.add(gameType);
		return gameType;
	}
	
	@Nullable
	public static GameType<?, ?> fromName(String name) {
		for (GameType<?, ?> gameType : GAME_TYPES) {
			if (gameType.getName().equals(name)) {
				return gameType;
			}
		}
		return null;
	}
	
}
