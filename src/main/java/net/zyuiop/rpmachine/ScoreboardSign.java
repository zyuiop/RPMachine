package net.zyuiop.rpmachine;

/**
 * @author zyuiop
 */
public interface ScoreboardSign {
	void create();

	void destroy();

	void setObjectiveName(String name);

	void setLine(int line, String value);

	void removeLine(int line);

	String getLine(int line);
}