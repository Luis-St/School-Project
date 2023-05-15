package net.luis.game.resources;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Luis-st
 *
 */

public record ResourceManager(@NotNull Path gameDirectory, @NotNull Path resourceDirectory) {
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ResourceManager that)) return false;
		
		if (!this.gameDirectory.equals(that.gameDirectory)) return false;
		return this.resourceDirectory.equals(that.resourceDirectory);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.gameDirectory, this.resourceDirectory);
	}
	
	@Override
	public String toString() {
		return "ResourceManager{gameDirectory=" + this.gameDirectory + ", resourceDirectory=" + this.resourceDirectory + "}";
	}
	//endregion
}
