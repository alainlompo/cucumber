package gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static gherkin.StringUtils.ltrim;
import static gherkin.SymbolCounter.countSymbols;

public class GherkinLine implements IGherkinLine {
    private final String lineText;
    private final String trimmedLineText;

    public GherkinLine(String lineText) {
        this.lineText = lineText;
        this.trimmedLineText = ltrim(lineText);
    }

    @Override
    public Integer indent() {
        return countSymbols(lineText) - countSymbols(trimmedLineText);
    }

    @Override
    public void detach() {
    	// Not implemented yet
    }

    @Override
    public String getLineText(int indentToRemove) {
        if (indentToRemove < 0 || indentToRemove > indent())
            return trimmedLineText;
        return lineText.substring(indentToRemove);
    }

    @Override
    public boolean isEmpty() {
        return trimmedLineText.length() == 0;
    }

    @Override
    public boolean startsWith(String prefix) {
        return trimmedLineText.startsWith(prefix);
    }

    @Override
    public String getRestTrimmed(int length) {
        return trimmedLineText.substring(length).trim();
    }

    @Override
    public List<GherkinLineSpan> getTags() {
        return getSpans("\\s+");
    }

    @Override
    public boolean startsWithTitleKeyword(String text) {
        int textLength = text.length();
        return trimmedLineText.length() > textLength &&
                trimmedLineText.startsWith(text) &&
                trimmedLineText.substring(textLength, textLength + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length())
                        .equals(GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR);
        // TODO aslak: extract startsWithFrom method for clarity
    }

    
    int processFirstCellOrAfter(StringBuilder cell, List<GherkinLineSpan> lineSpans, int col, int startCol) {
    	int contentStart = 0;
        while (contentStart < cell.length() && Character.isWhitespace(cell.charAt(contentStart))) {
            contentStart++;
        }
        if (contentStart == cell.length()) {
            contentStart = 0;
        }
        lineSpans.add(new GherkinLineSpan(indent() + startCol + contentStart + 2, cell.toString().trim()));
        return col;
    }
    
    ProcessState processVerticalBar(StringBuilder cell, List<GherkinLineSpan> lineSpans, boolean beforeFirst,int col, int startCol ) {
    	boolean beforeFirstUpdate = beforeFirst;
    	int startColUpdate = startCol;
    	if (beforeFirst) {
            // Skip the first empty span
    		beforeFirstUpdate = false;
        } else {
            startColUpdate = processFirstCellOrAfter(cell, lineSpans, col, startCol);
        }
        return new ProcessState(beforeFirstUpdate, startColUpdate, new StringBuilder());
    }
    
    ProcessState processAntiSlash(StringBuilder cell, int col) {
    	int colUpdate =col + 1;
        char c = trimmedLineText.charAt(colUpdate);
        if (c == 'n') {
            cell.append('\n');
        } else {
            if (c != '|' && c != '\\') {
                cell.append('\\');
            }
            cell.append(c);
        }
        return new ProcessState(colUpdate, cell);
    }
    
    @Override
    public List<GherkinLineSpan> getTableCells() {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean beforeFirst = true;
        int startCol = 0;
        int col = -1;
        do {
        	col++;
        	char c = trimmedLineText.charAt(col);
            if (c == '|') {
                ProcessState state = processVerticalBar(cell, lineSpans, beforeFirst, col, startCol);
                startCol = state.getStartCol();
                beforeFirst = state.isBeforeFirst();
                cell = state.getCell();
            } else if (c == '\\') {
                col = processAntiSlash(cell, col).getCol();
            } else {
                cell.append(c);
            }
        } while (col < trimmedLineText.length());
        return lineSpans;
    }

    private List<GherkinLineSpan> getSpans(String delimiter) {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(trimmedLineText).useDelimiter(delimiter);) {
	        
	        while (scanner.hasNext()) {
	            String cell = scanner.next();
	            int column = scanner.match().start() + indent() + 1;
	            lineSpans.add(new GherkinLineSpan(column, cell));
	        }
	        
	        return lineSpans;
        } catch (NullPointerException npe) {
        	// Handle NPE here
        	return new ArrayList<>();
        }  
    }
    
    class ProcessState {
    	private boolean beforeFirst;
    	private int startCol;
    	private int col;
    	private StringBuilder cell;

		public ProcessState(boolean beforeFirst, int startCol, StringBuilder cell) {
			super();
			this.beforeFirst = beforeFirst;
			this.startCol = startCol;
			this.cell = cell;
		}
		
		public ProcessState(int col, StringBuilder cell) {
			super();
			this.col = col;
			this.cell = cell;
		}

		public boolean isBeforeFirst() {
			return beforeFirst;
		}

		public void setBeforeFirst(boolean beforeFirst) {
			this.beforeFirst = beforeFirst;
		}

		public int getStartCol() {
			return startCol;
		}

		public void setStartCol(int startCol) {
			this.startCol = startCol;
		}

		public StringBuilder getCell() {
			return cell;
		}

		public void setCell(StringBuilder cell) {
			this.cell = cell;
		}

		public int getCol() {
			return col;
		}

		public void setCol(int col) {
			this.col = col;
		}
    }
}
