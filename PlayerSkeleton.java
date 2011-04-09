public class PlayerSkeleton {

	int simulationField[][] = new int[State.ROWS][State.COLS];
	int simulationTop[] = new int[State.COLS];

	public int pickMove(State s, int[][] legalMoves) {
		// evaluate all possible moves to see which one is best
		int bestMove = 0, orient = 0, slot = 0;
		double highestEvaluation = Integer.MIN_VALUE, evaluation = Integer.MIN_VALUE;

		for (int i = 0; i < legalMoves.length; i++) {
			orient = legalMoves[i][0];
			slot = legalMoves[i][1];

			// Simulate and evaluate the move
			if (simulatePlayingMove(s, orient, slot, simulationField,
					simulationTop)) {
				evaluation = getLandingHeight(s, orient, slot) * -1
						+ getNumberOfHoles(simulationField, simulationTop) * -4
						+ rowsCleared + getRowTransitions(simulationField) * -1
						+ getColumnTransitions(simulationField) * -1
						+ getWellSums(simulationField) * -1;
			} else {
				// the simulation failed (i.e. we lost the game)
				evaluation = Integer.MIN_VALUE;
			}

			if (evaluation > highestEvaluation) {
				highestEvaluation = evaluation;
				bestMove = i;
			}
		}

		return bestMove;
	}

	// Get landing height of a piece
	private double getLandingHeight(State s, int orient, int slot) {
		int top[] = s.getTop();
		int pBottom[][][] = State.getpBottom();
		int nextPiece = s.getNextPiece();
		int pWidth[][] = State.getpWidth();
		int pieceHeight = State.getpHeight()[nextPiece][orient];
		
		// get the height of the highest column under the piece.
		int height = top[slot] - pBottom[nextPiece][orient][0];

		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}
		
		return 0.5 * (height + (height + pieceHeight));
	}

	/**
	 * Get the total number of holes. A hole is an empty cell that has at least
	 * one filled cell above it in the column.
	 */
	private int getNumberOfHoles(int field[][], int top[]) {
		int numHoles = 0;

		for (int columnIndex = 0; columnIndex < State.COLS; columnIndex++) {
			for (int rowIndex = Math.min(top[columnIndex] - 1, State.ROWS - 2); rowIndex >= 0; rowIndex--) {
				if (field[rowIndex][columnIndex] == 0)
					numHoles++;
			}
		}

		return numHoles;
	}

	int getRowTransitions(int field[][]) {
		int rowTransitions = 0;
		for (int rowIndex = 0; rowIndex < field.length; rowIndex++) {
			if (field[rowIndex][0] == 0) {
				rowTransitions++;
			} else if (field[rowIndex][field[0].length - 1] == 0) {
				rowTransitions++;
			}

			for (int columnIndex = 1; columnIndex < field[0].length; columnIndex++) {
				if ((field[rowIndex][columnIndex] == 0)
						^ (field[rowIndex][columnIndex - 1] == 0)) {
					rowTransitions++;
				}
			}
		}

		return rowTransitions;
	}

	int getColumnTransitions(int field[][]) {
		int columnTransitions = 0;
		for (int columnIndex = 0; columnIndex < field[0].length; columnIndex++) {
			if (field[0][columnIndex] == 0) {
				columnTransitions++;
			}

			for (int rowIndex = 1; rowIndex < field.length; rowIndex++) {
				if ((field[rowIndex - 1][columnIndex] == 0)
						^ (field[rowIndex][columnIndex] == 0)) {
					columnTransitions++;
				}
			}
		}

		return columnTransitions;
	}

	public void multiArrayCopy(int[][] source, int[][] destination) {
		for (int a = 0; a < source.length; a++) {
			System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
		}
	}

	public void arrayCopy(int source[], int destination[]) {
		for (int i = 0; i < source.length; i++) {
			destination[i] = source[i];
		}
	}

	
	int rowsCleared = 0;

	// Simulate the effect of playing the next piece.
	// Returns the field of the game as if that piece has been played.
	// The piece's cells are have value -1 in the field grid.
	// Returns true if we did not lose in the simulation, false otherwise.
	private boolean simulatePlayingMove(State s, int orient, int slot,
			int field[][], int top[]) {
		int nextPiece = s.getNextPiece();
		int[][][] pBottom = State.getpBottom();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();
		int[][][] pTop = State.getpTop();
		multiArrayCopy(s.getField().clone(), field);
		arrayCopy(s.getTop(), top);

		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];
		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}

		// check if game ended
		if (height + pHeight[nextPiece][orient] >= State.ROWS) {
			// lost = true;
			return false;
		}

		// for each column in the piece - fill in the appropriate blocks
		for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

			// from bottom to top of brick
			for (int h = height + pBottom[nextPiece][orient][i]; h < height
					+ pTop[nextPiece][orient][i]; h++) {
				field[h][i + slot] = -1;
			}
		}

		// adjust top
		for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot + c] = height + pTop[nextPiece][orient][c];
		}

		rowsCleared = 0;

		// check for full rows - starting at the top
		for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
			// check all columns in the row
			boolean full = true;
			for (int c = 0; c < State.COLS; c++) {
				if (field[r][c] == 0) {
					full = false;
					break;
				}
			}
			// if the row was full - remove it and slide above stuff down
			if (full) {
				rowsCleared++;
				// for each column
				for (int c = 0; c < State.COLS; c++) {

					// slide down all bricks
					for (int i = r; i < top[c]; i++) {
						field[i][c] = field[i + 1][c];
					}
					// lower the top
					top[c]--;
					while (top[c] >= 1 && field[top[c] - 1][c] == 0)
						top[c]--;
				}
			}
		}

		return true;
	}

	public int getWellSums(int field[][]) {
		int wellCells = 0;
		int wellWeights = 0;

		for (int rowIndex = 0; rowIndex < field.length; rowIndex++) {
			if (field[rowIndex][0] == 0 && (field[rowIndex][1] != 0)) {
				wellCells++;
				// found well cell. Count how many rows beneath the cell and add
				// it to the well weight
				for (int i = rowIndex - 1; i >= 0 && field[i][0] == 0; i--) {
					wellWeights++;
				}
			}
		}

		for (int rowIndex = 0; rowIndex < field.length; rowIndex++) {
			if (field[rowIndex][field[0].length - 1] == 0
					&& (field[rowIndex][field[0].length - 2] != 0)) {
				wellCells++;
				// found well cell. Count how many rows beneath the cell and add
				// it to the well weight
				for (int i = rowIndex - 1; i >= 0
						&& field[i][field[0].length - 1] == 0; i--) {
					wellWeights++;
				}
			}
		}

		for (int columnIndex = 1; columnIndex < field[0].length - 1; columnIndex++) {
			for (int rowIndex = 0; rowIndex < field.length; rowIndex++) {
				if (field[rowIndex][columnIndex] == 0
						&& (field[rowIndex][columnIndex - 1] != 0)
						&& (field[rowIndex][columnIndex + 1] != 0)) {
					// found well cell. Count how many rows beneath the cell and
					// add it to the well weight
					for (int i = rowIndex - 1; i >= 0
							&& field[i][columnIndex] == 0; i--) {
						wellWeights++;
					}
					wellCells++;
				}
			}
		}

		return wellCells + wellWeights;
	}

	public static void main(String[] args) {
		State s = new State();
		s.setSeed(0);
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));

//			s.draw();
//			s.drawNext(0, 0);
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		
		s.draw();
		s.drawNext(0, 0);
		System.out.println("You have turns " + s.getTurnNumber() + ".");
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
	}

}