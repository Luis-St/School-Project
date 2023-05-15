package net.luis.game.map;

import com.google.common.collect.ImmutableList;
import net.luis.game.Game;
import net.luis.game.application.ApplicationType;
import net.luis.game.map.field.GameField;
import net.luis.game.map.field.GameFieldPos;
import net.luis.game.map.field.GameFieldType;
import net.luis.game.player.game.GamePlayer;
import net.luis.game.player.game.GamePlayerType;
import net.luis.game.player.game.figure.GameFigure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 *
 * @author Luis-st
 *
 */

public interface GameMap {
	
	default void init() {
	
	}
	
	default void init(@NotNull List<GamePlayer> players) {
		if (ApplicationType.CLIENT.isOn()) {
			this.getFields().forEach(GameField::clear);
		}
	}
	
	void addFields();
	
	@NotNull Game getGame();
	
	@NotNull List<GameField> getFields();
	
	default @NotNull List<GameField> getFields(Predicate<GameField> predicate) {
		return this.getFields().stream().filter(predicate).collect(ImmutableList.toImmutableList());
	}
	
	default @Nullable GameField getField(@NotNull GameFigure figure) {
		for (GameField field : this.getFields()) {
			if (!field.isEmpty() && Objects.equals(field.getFigure(), figure)) {
				return field;
			}
		}
		return null;
	}
	
	@Nullable GameField getField(@Nullable GameFieldType fieldType, @Nullable GamePlayerType playerType, @NotNull GameFieldPos fieldPos);
	
	@Nullable GameField getNextField(@NotNull GameFigure figure, int count);
	
	@NotNull List<GameField> getHomeFields(@NotNull GamePlayerType playerType);
	
	@NotNull List<GameField> getStartFields(@NotNull GamePlayerType playerType);
	
	@NotNull List<GameField> getWinFields(@NotNull GamePlayerType playerType);
	
	default boolean hasEmptyField() {
		for (GameField field : this.getFields()) {
			if (field.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	default @Nullable GameFigure getFigure(@NotNull GamePlayer player, int figure) {
		return player.getFigure(figure);
	}
	
	default boolean moveFigure(@NotNull GameFigure figure, int count) {
		GameField field = this.getNextField(figure, count);
		if (field != null) {
			return this.moveFigureTo(figure, field);
		}
		return false;
	}
	
	default boolean moveFigureTo(@NotNull GameFigure figure, @NotNull GameField field) {
		return false;
	}
	
	default @Nullable GameField getSelectedField() {
		return null;
	}
	
	default void reset() {
		this.getFields().forEach(GameField::clear);
	}
}
