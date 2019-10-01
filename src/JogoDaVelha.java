import java.util.Random;

public class JogoDaVelha implements Cloneable {
	/**
	 * V�rias constantes usadas para rastrear o estado da grade ou o jogador ativo.
	 */
	// Constantes usadas para se referir a uma jogada feita por jogador ou
	// computador.
	private final int NOBODY_TURN = 0;
	private final int PLAYER_TURN = 1;
	private final int COMPUTER_TURN = -1;

	// A marca que indica na grade impressa os movimentos feitos pelo jogador ou
	// pelo computador.
	private final char NOBODY_MARK = ' ';
	private final char PLAYER_MARK = 'X';
	private final char COMPUTER_MARK = 'O';

	// A dificuldade do jogo. Este valor � usado por bestGuess ().
	private final int GAME_LEVEL = 8;

	/**
	 * Um n�mero inteiro (usando as constantes ..._ TURN acima) descrevendo de quem
	 * � a vez para fazer um movimento.
	 */
	private int whoseTurn = NOBODY_TURN;

	/**
	 * Um n�mero inteiro (usando as constantes ..._ TURN acima) descrevendo quem
	 * iniciar� o jogos.
	 */
	private int firstTurn = NOBODY_TURN;

	/**
	 * Uma matriz que representa o estado do tabuleiro do jogo atual como uma grade.
	 */
	private char[] grid = new char[9];

	/**
	 * Uma matriz que representa o conjunto atual de movimentos feitos neste jogo.
	 * movimentos [i] conter� 0 se a grade i n�o estiver dispon�vel ou 1 se estiver
	 * dispon�vel.
	 */
	private int[] moves = new int[9];

	/**
	 * Representa quantas jogadas foram feitas durante o decorrer do jogo.
	 */
	private int numMoves = 0;

	/**
	 * Estabelece um novo estado de jogo limpando o tabuleiro.
	 */
	public JogoDaVelha() {
		// Preencha o quadro para que fique vazio.
		for (int i = 0; i < 9; i++) {
			grid[i] = NOBODY_MARK;
		}

		generateMoves();
	}

	/**
	 * Retorna um clone deste objeto.
	 */
	public JogoDaVelha clone() throws CloneNotSupportedException {
		// � necess�rio clonar as matrizes, pois elas s�o tratadas como objetos em Java.
		JogoDaVelha clone = (JogoDaVelha) super.clone();
		clone.grid = this.grid.clone();
		clone.moves = this.moves.clone();

		return clone;
	}

	/**
	 * Determina aleatoriamente quem ser� o primeiro neste jogo e retornar� uma
	 * String descrevendo o jogador.
	 */
	public void chooseFirstPlayer() {
		// Determine aleatoriamente se o player ou o computador ser� o primeiro a
		// funcionar durante
		// este jogo.
		Random generator = new Random();

		if (generator.nextInt(2) == 0) {
			setFirstTurn(PLAYER_TURN);
		}

		else {
			setFirstTurn(COMPUTER_TURN);
		}
	}

	/**
	 * Retorna uma String descrevendo o estado do quadro. Essa string pode ser usada
	 * pelo cliente para desenhar o quadro.
	 */
	public String drawBoard() {
		String toReturn = "";

		for (char space : grid) {
			if (space == PLAYER_MARK)
				toReturn += "1";
			else if (space == COMPUTER_MARK)
				toReturn += "2";
			else
				toReturn += "-";
		}

		return toReturn + "\n";
	}

	/**
	 * Retorna uma matriz contendo uma lista de todos os movimentos poss�veis para
	 * esse estado do jogo.
	 */
	public int[] generateMoves() {
		// Gere a lista de movimentos poss�veis e armazene-os em movimentos.
		for (int i = 0; i < moves.length; i++) {
			// Caso contr�rio, marque o quadrado como dispon�vel. Caso contr�rio, �
			// indispon�vel.
			moves[i] = (grid[i] == NOBODY_MARK) ? 1 : 0;
		}

		return moves;
	}

	/**
	 * Retorna uma matriz que cont�m uma lista de todos os movimentos legais
	 * restantes para esse estado do jogo.
	 */
	public int[] generateLegalMoves() {
		// Crie a lista de movimentos legais do conjunto de movimentos.
		int[] legalMoves = new int[0];

		for (int i = 0; i < moves.length; i++) {
			// Se a movimenta��o estiver dispon�vel, adicione-a � matriz de movimenta��es
			// legais.
			if (moves[i] == 1) {
				// Aumente o tamanho da matriz de movimentos legais.
				int[] tempLegalMoves = legalMoves;
				legalMoves = new int[tempLegalMoves.length + 1];

				for (int j = 0; j < tempLegalMoves.length; j++) {
					legalMoves[j] = tempLegalMoves[j];
				}

				legalMoves[legalMoves.length - 1] = i;
			}
		}

		return legalMoves;
	}

	/**
	 * Retorna um valor booleano sobre se uma movimenta��o proposta � legal ou n�o.
	 */
	public boolean legalMove(int move) {
		// Retorne false se o movimento estiver fora dos limites do jogo.
		if (move < 0 || move > 8) {
			return false;
		}

		// Retorna true se a movimenta��o estiver dispon�vel e false se n�o estiver.
		return moves[move] == 1;
	}

	/**
	 * O m�todo pelo qual o computador gera sua movimenta��o. Este m�todo ir� tamb�m
	 * fa�a a mudan�a para o computador.
	 */
	public void computerMove() throws CloneNotSupportedException {
		int computerMove = bestMove();

		placePiece(COMPUTER_TURN, computerMove);
	}

	/**
	 * Tentativas de determinar a melhor jogada a ser feita pelo computador com base
	 * no estado atual da placa. Retorna a melhor jogada poss�vel como um n�mero
	 * inteiro.
	 * 
	 * @return int
	 * @throws CloneNotSupportedException se os objetos JogoDaVelha n�o forem clonados
	 *                                    com sucesso.
	 */
	public int bestMove() throws CloneNotSupportedException {
		/**
		 * Acompanha o valor da estimativa da melhor jogada e o valor da estimativa
		 * movimento atual sendo examinado.
		 */
		int bestGuessValue, currentGuessValue;

		/**
		 * Armazena a melhor jogada encontrada at� agora e a pr�xima jogada a tentar.
		 */
		int best, tryMove;

		/**
		 * Possui uma c�pia do jogo atual.
		 */
		JogoDaVelha tempSituation;

		/**
		 * Armazena o conjunto de movimentos legais para esse estado da placa.
		 */
		int[] legalMoves;

		// Copie a placa atual e gere uma lista de movimentos legais no computador
		// tentar� fazer.
		tempSituation = this.clone();
		legalMoves = tempSituation.generateLegalMoves();

		// Comece a determinar a melhor jogada na primeira jogada legal do conjunto.
		// Coloque o
		// pe�a e determine um
		// estimativa da probabilidade de o computador vencer o jogo fazendo-o.
		tryMove = legalMoves[0];

		tempSituation.placePiece(COMPUTER_TURN, tryMove);
		bestGuessValue = tempSituation.bestGuess(GAME_LEVEL);

		// Acompanhe a melhor jogada que o computador pode fazer. Por enquanto, tem que
		// ser o primeiro
		// movimento legal.
		best = tryMove;

		// Tente todos os movimentos legais poss�veis para determinar qual � o melhor
		// gosta mais com base no
		// estado atual da placa.
		int currentMoveIndex = 1;
		while (currentMoveIndex < legalMoves.length) {
			// Apague a pe�a colocada anteriormente e tente os demais movimentos legais para
			// determina qual d� o melhor
			// resultado.
			tempSituation = this.clone();
			tryMove = legalMoves[currentMoveIndex];

			tempSituation.placePiece(COMPUTER_TURN, tryMove);

			// Determine a chance do computador vencer fazendo essa jogada.
			currentGuessValue = tempSituation.bestGuess(GAME_LEVEL);

			// Escolha a jogada que oferece ao computador a maior chance de ganhar.
			// Fa�a isso movendo-se com o maior valor de palpite (o mais pr�ximo de ganhar
			// 100) se o computador ligar.
			// Caso contr�rio, "bloqueie" a jogada que permitiria ao jogador vencer (o mais
			// pr�ximo
			// 0).
			if ((tempSituation.getWhoseTurn() == COMPUTER_TURN && currentGuessValue > bestGuessValue)
					|| tempSituation.getWhoseTurn() != COMPUTER_TURN && currentGuessValue < bestGuessValue) {
				bestGuessValue = currentGuessValue;
				best = tryMove;
			}

			currentMoveIndex++;
		}

		return best;
	}

	/**
	 * Usado pelo bestMove para examinar o benef�cio que uma mudan�a ter� com base
	 * no estado atual do quadro. Retorna um valor inteiro que descreve a
	 * probabilidade de vit�ria do computador com base no estado do tabuleiro do
	 * jogo que est� invocando.
	 *
	 * 100 - computador ganhou 50 - jogo empatado 0 - computador perdeu
	 *
	 * O n�vel descreve quanto mais o jogo ser� jogado para determinar a melhor
	 * jogada poss�vel. Os valores do n�vel variam de 0 a 8, sendo 8 tecnicamente
	 * imbat�veis.
	 */
	public int bestGuess(int level) throws CloneNotSupportedException {
		/**
		 * Acompanha o valor da estimativa da melhor jogada e o valor da estimativa
		 * atual que est� sendo examinada.
		 */
		int bestGuessValue, currentGuessValue;

		/**
		 * Armazena a pr�xima jogada para tentar.
		 */
		int tryMove;

		/**
		 * Possui uma c�pia do jogo atual.
		 */
		JogoDaVelha tempSituation;

		/**
		 * Possui uma c�pia dos movimentos legais para este jogo.
		 */
		int[] legalMoves = this.generateLegalMoves();

		// Se estivermos no n�vel b�sico ou o jogo terminar, retorne o desempenho do
		// computador.
		if ((level == 0 || this.isOver())) {
			return judge();
		}

		// Copie o jogo atual.
		tempSituation = this.clone();

		// Se o n�vel estiver nivelado, o computador estar� tentando uma mudan�a.
		if (level % 2 == 0) {
			tempSituation.setWhoseTurn(COMPUTER_TURN);
		}

		// Se o n�vel for �mpar, o jogador estar� tentando uma jogada.
		else {
			tempSituation.setWhoseTurn(PLAYER_TURN);
		}

		// Coloque a primeira jogada legal e determine o qu�o boa � para o computador.
		tryMove = legalMoves[0];
		tempSituation.placePiece(tempSituation.getWhoseTurn(), tryMove);
		bestGuessValue = tempSituation.bestGuess(level - 1);

		// Execute os movimentos legais restantes.
		int currentMoveIndex = 1;
		while (currentMoveIndex < legalMoves.length) {
			// Apague a jogada anterior e tente a pr�xima dispon�vel.
			tempSituation = this.clone();
			tryMove = legalMoves[currentMoveIndex];
			tempSituation.placePiece(tempSituation.getWhoseTurn(), tryMove);

			currentGuessValue = tempSituation.bestGuess(level - 1);

			// Se for a vez do jogador, queremos considerar essa a��o para impedi-lo de
			// vencer.
			if (tempSituation.getWhoseTurn() == PLAYER_TURN) {
				bestGuessValue = Math.max(bestGuessValue, currentGuessValue);
			}

			// Se for a vez do computador, queremos tomar a decis�o que pode vencer.
			else {
				bestGuessValue = Math.min(bestGuessValue, currentGuessValue);
			}

			currentMoveIndex++;
		}

		return bestGuessValue;
	}

	/**
	 * Atualiza o jogo para que o jogador ({player} _TURN) fa�a um movimento no
	 * especificado ponto na grade.
	 */
	public void placePiece(int player, int move) {
		grid[move] = (player == PLAYER_TURN) ? PLAYER_MARK : COMPUTER_MARK;

		numMoves++;

		// Gere o novo conjunto de jogadas executadas/n�o executadas no jogo.
		generateMoves();
	}

	/**
	 * Retorna um valor inteiro com base no exame do estado do jogo: 0 - o jogo est�
	 * em andamento 1 - o jogador ganhou 2 - o computaor ganhou 3 - o jogo est�
	 * empatado
	 */
	public int result() {
		// Primeiro verifique as colunas para ver se existem vencedores.
		for (int i = 0; i < 3; i++) {
			// O computador ganhou?
			if (grid[i] == PLAYER_MARK && grid[i + 3] == PLAYER_MARK && grid[i + 6] == PLAYER_MARK) {
				return 1;
			}

			// O computador ganhou?
			else if (grid[i] == COMPUTER_MARK && grid[i + 3] == COMPUTER_MARK && grid[i + 6] == COMPUTER_MARK) {
				return 2;
			}
		}

		// Em seguida, verifique as linhas para quaisquer vencedores.
		for (int i = 0; i <= 6; i += 3) {
			// O computador ganhou?
			if (grid[i] == PLAYER_MARK && grid[i + 1] == PLAYER_MARK && grid[i + 2] == PLAYER_MARK) {
				return 1;
			}

			// O computador ganhou?
			if (grid[i] == COMPUTER_MARK && grid[i + 1] == COMPUTER_MARK && grid[i + 2] == COMPUTER_MARK) {
				return 2;
			}
		}

		// Por fim, verifique as diagonais.
		if (grid[0] == PLAYER_MARK && grid[4] == PLAYER_MARK && grid[8] == PLAYER_MARK) {
			return 1;
		}

		else if (grid[2] == PLAYER_MARK && grid[4] == PLAYER_MARK && grid[6] == PLAYER_MARK) {
			return 1;
		}

		if (grid[0] == COMPUTER_MARK && grid[4] == COMPUTER_MARK && grid[8] == COMPUTER_MARK) {
			return 2;
		}

		else if (grid[2] == COMPUTER_MARK && grid[4] == COMPUTER_MARK && grid[6] == COMPUTER_MARK) {
			return 2;
		}

		// Se houver 9 jogadas nesse momento, o jogo ser� empatado.
		if (numMoves == 9) {
			return 3;
		}

		// Caso contr�rio, o jogo continua!
		else {
			return 0;
		}
	}

	/**
	 * Retorna um valor inteiro que descreve a qualidade da situa��o atual do
	 * computador. 100: o computador ganhou 50: computador / jogador est� empatado
	 * 0: o jogador ganhou
	 */
	public int judge() {
		switch (result()) {
		case 0:
			return 50;
		case 1:
			return 0;
		case 2:
			return 100;
		case 3:
			return 50;
		}

		// Nunca deve chegar aqui ...
		return 50;
	}

	/**
	 * Retorna true se o jogo acabou ou false se n�o estiver.
	 */
	public boolean isOver() {
		return (result() != 0);
	}

	/**
	 * Retorne o valor int de quem controla o turno atual.
	 */
	public int getWhoseTurn() {
		return whoseTurn;
	}

	/**
	 * Define o valor int de quem controla o turno atual.
	 */
	public void setWhoseTurn(int whoseTurn) {
		this.whoseTurn = whoseTurn;
	}

	/**
	 * Retorne o valor int de quem ser� o primeiro neste jogo.
	 */
	public int getFirstTurn() {
		return firstTurn;
	}

	/**
	 * Define o valor de quem ser� o primeiro neste jogo.
	 */
	public void setFirstTurn(int firstTurn) {
		this.firstTurn = firstTurn;
	}

	/**
	 * Retorna uma matriz de movimentos validos.
	 */
	public int[] getMoves() {
		return moves;
	}

	/**
	 * Retorna o valor int que representa o jogador que fez o primeiro movimento do
	 * jogo.
	 */
	public int getPLAYER_TURN() {
		return PLAYER_TURN;
	}

	/**
	 * Retorna o valor int que representa o computador que tem a primeira jogada do
	 * jogo.
	 */
	public int getCOMPUTER_TURN() {
		return COMPUTER_TURN;
	}
}
