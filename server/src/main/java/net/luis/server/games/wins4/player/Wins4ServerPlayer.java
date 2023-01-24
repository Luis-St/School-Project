package net.luis.server.games.wins4.player;

import com.google.common.collect.Lists;
import net.luis.game.Game;
import net.luis.game.map.field.GameFieldPos;
import net.luis.game.player.GamePlayer;
import net.luis.game.player.GamePlayerType;
import net.luis.game.player.figure.GameFigure;
import net.luis.game.player.Player;
import net.luis.server.game.player.AbstractServerGamePlayer;
import net.luis.server.games.wins4.player.figure.Wins4ServerFigure;
import net.luis.utils.util.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Luis-st
 *
 */

public class Wins4ServerPlayer extends AbstractServerGamePlayer {
	
	private final List<GameFigure> figures;
	
	public Wins4ServerPlayer(Game game, Player player, GamePlayerType playerType) {
		super(game, player, playerType);
		this.figures = createFigures(this, playerType);
	}
	
	private static List<GameFigure> createFigures(GamePlayer player, GamePlayerType type) {
		List<GameFigure> figures = Lists.newArrayList();
		for (int i = 0; i < 21; i++) {
			figures.add(new Wins4ServerFigure(player, i, UUID.randomUUID()));
		}
		return figures;
	}
	
	@Override
	public List<GameFigure> getFigures() {
		return this.figures;
	}
	
	@Override
	public List<GameFieldPos> getWinPoses() {
		return Lists.newArrayList();
	}
	
	@Override
	public final int getRollCount() {
		GamePlayer.LOGGER.warn("Fail to get roll count of player {}, since the 4 wins is not a dice game", this.getPlayer().getProfile().getName());
		return -1;
	}
	
	@Override
	public final void setRollCount(int rollCount) {
		GamePlayer.LOGGER.warn("Fail to set roll count of player {} to {}, since the 4 wins is not a dice game", this.getPlayer().getProfile().getName(), rollCount);
	}
	
	@Nullable
	public GameFigure getUnplacedFigure() {
		for (GameFigure figure : this.figures) {
			if (this.getMap().getField(figure) == null) {
				return figure;
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!super.equals(object)) {
			return false;
		} else if (object instanceof Wins4ServerPlayer player) {
			return this.figures.equals(player.figures);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
	
}
