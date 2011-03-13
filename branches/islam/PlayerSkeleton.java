
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		// evaluate all possible moves to see which one is best
		
		double highestEvaluation = -100;
		int bestMove = -1;
		for (int i = 0; i < legalMoves.length; i++) {
			double evaluation = getLandingHeight(s, legalMoves[i][0], legalMoves[i][1]) * -1;
			System.err.println(evaluation);
			if (evaluation > highestEvaluation) {
				highestEvaluation = evaluation;
				bestMove = i;
			}
		}
		
		return bestMove;
	}
	
	// Get landing height of a piece
	private double getLandingHeight(State s, int orient, int slot) {
		// get the height of the highest column under the piece.
		int top[] = s.getTop();
		int pBottom[][][] = State.getpBottom();
		int nextPiece = s.getNextPiece();
		int pWidth[][] = State.getpWidth();
		
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		int pieceHeight = State.getpHeight()[nextPiece][orient];
		
		return 0.5 * (height + (height + pieceHeight));
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
