import java.util.Random;

public class JogoDaVelha implements Cloneable {
	/**
	 * Várias constantes usadas para rastrear o estado da grade ou o jogador ativo.
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

	// A dificuldade do jogo. Este valor é usado por bestGuess ().
	private final int GAME_LEVEL = 8;

	/**
	 * Um número inteiro (usando as constantes ..._ TURN acima) descrevendo de quem
	 * é a vez para fazer um movimento.
	 */
	private int whoseTurn = NOBODY_TURN;

	/**
	 * Um número inteiro (usando as constantes ..._ TURN acima) descrevendo quem
	 * iniciará o jogos.
	 */
	private int firstTurn = NOBODY_TURN;

	/**
	 * Uma matriz que representa o estado do tabuleiro do jogo atual como uma grade.
	 */
	private char[] grid = new char[9];

	/**
	 * Uma matriz que representa o conjunto atual de movimentos feitos neste jogo.
	 * movimentos [i] conterá 0 se a grade i não estiver disponível ou 1 se estiver
	 * disponível.
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
		// É necessário clonar as matrizes, pois elas são tratadas como objetos em Java.
		JogoDaVelha clone = (JogoDaVelha) super.clone();
		clone.grid = this.grid.clone();
		clone.moves = this.moves.clone();

		return clone;
	}

	/**
	 * Determina aleatoriamente quem será o primeiro neste jogo e retornará uma
	 * String descrevendo o jogador.
	 */
	public void chooseFirstPlayer() {
		// Determine aleatoriamente se o player ou o computador será o primeiro a
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
	 * Retorna uma matriz contendo uma lista de todos os movimentos possíveis para
	 * esse estado do jogo.
	 */
	public int[] generateMoves() {
		// Gere a lista de movimentos possíveis e armazene-os em movimentos.
		for (int i = 0; i < moves.length; i++) {
			// Caso contrário, marque o quadrado como disponível. Caso contrário, é
			// indisponível.
			moves[i] = (grid[i] == NOBODY_MARK) ? 1 : 0;
		}

		return moves;
	}

	/**
	 * Retorna uma matriz que contém uma lista de todos os movimentos legais
	 * restantes para esse estado do jogo.
	 */
	public int[] generateLegalMoves() {
		// Crie a lista de movimentos legais do conjunto de movimentos.
		int[] legalMoves = new int[0];

		for (int i = 0; i < moves.length; i++) {
			// Se a movimentação estiver disponível, adicione-a à matriz de movimentações
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
	 * Retorna um valor booleano sobre se uma movimentação proposta é legal ou não.
	 */
	public boolean legalMove(int move) {
		// Retorne false se o movimento estiver fora dos limites do jogo.
		if (move < 0 || move > 8) {
			return false;
		}

		// Retorna true se a movimentação estiver disponível e false se não estiver.
		return moves[move] == 1;
	}

	/**
	 * O método pelo qual o computador gera sua movimentação. Este método irá também
	 * faça a mudança para o computador.
	 */
	public void computerMove() throws CloneNotSupportedException {
		int computerMove = bestMove();

		placePiece(COMPUTER_TURN, computerMove);
	}

	/**
	 * Tentativas de determinar a melhor jogada a ser feita pelo computador com base
	 * no estado atual da placa. Retorna a melhor jogada possível como um número
	 * inteiro.
	 * 
	 * @return int
	 * @throws CloneNotSupportedException se os objetos JogoDaVelha não forem clonados
	 *                                    com sucesso.
	 */
	public int bestMove() throws CloneNotSupportedException {
		/**
		 * Acompanha o valor da estimativa da melhor jogada e o valor da estimativa
		 * movimento atual sendo examinado.
		 */
		int bestGuessValue, currentGuessValue;

		/**
		 * Armazena a melhor jogada encontrada até agora e a próxima jogada a tentar.
		 */
		int best, tryMove;

		/**
		 * Possui uma cópia do jogo atual.
		 */
		JogoDaVelha tempSituation;

		/**
		 * Armazena o conjunto de movimentos legais para esse estado da placa.
		 */
		int[] legalMoves;

		// Copie a placa atual e gere uma lista de movimentos legais no computador
		// tentará fazer.
		tempSituation = this.clone();
		legalMoves = tempSituation.generateLegalMoves();

		// Comece a determinar a melhor jogada na primeira jogada legal do conjunto.
		// Coloque o
		// peça e determine um
		// estimativa da probabilidade de o computador vencer o jogo fazendo-o.
		tryMove = legalMoves[0];

		tempSituation.placePiece(COMPUTER_TURN, tryMove);
		bestGuessValue = tempSituation.bestGuess(GAME_LEVEL);

		// Acompanhe a melhor jogada que o computador pode fazer. Por enquanto, tem que
		// ser o primeiro
		// movimento legal.
		best = tryMove;

		// Tente todos os movimentos legais possíveis para determinar qual é o melhor
		// gosta mais com base no
		// estado atual da placa.
		int currentMoveIndex = 1;
		while (currentMoveIndex < legalMoves.length) {
			// Apague a peça colocada anteriormente e tente os demais movimentos legais para
			// determina qual dá o melhor
			// resultado.
			tempSituation = this.clone();
			tryMove = legalMoves[currentMoveIndex];

			tempSituation.placePiece(COMPUTER_TURN, tryMove);

			// Determine a chance do computador vencer fazendo essa jogada.
			currentGuessValue = tempSituation.bestGuess(GAME_LEVEL);

			// Escolha a jogada que oferece ao computador a maior chance de ganhar.
			// Faça isso movendo-se com o maior valor de palpite (o mais próximo de ganhar
			// 100) se o computador ligar.
			// Caso contrário, "bloqueie" a jogada que permitiria ao jogador vencer (o mais
			// próximo
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
	 * Usado pelo bestMove para examinar o benefício que uma mudança terá com base
	 * no estado atual do quadro. Retorna um valor inteiro que descreve a
	 * probabilidade de vitória do computador com base no estado do tabuleiro do
	 * jogo que está invocando.
	 *
	 * 100 - computador ganhou 50 - jogo empatado 0 - computador perdeu
	 *
	 * O nível descreve quanto mais o jogo será jogado para determinar a melhor
	 * jogada possível. Os valores do nível variam de 0 a 8, sendo 8 tecnicamente
	 * imbatíveis.
	 */
	public int bestGuess(int level) throws CloneNotSupportedException {
		/**
		 * Acompanha o valor da estimativa da melhor jogada e o valor da estimativa
		 * atual que está sendo examinada.
		 */
		int bestGuessValue, currentGuessValue;

		/**
		 * Armazena a próxima jogada para tentar.
		 */
		int tryMove;

		/**
		 * Possui uma cópia do jogo atual.
		 */
		JogoDaVelha tempSituation;

		/**
		 * Possui uma cópia dos movimentos legais para este jogo.
		 */
		int[] legalMoves = this.generateLegalMoves();

		// Se estivermos no nível básico ou o jogo terminar, retorne o desempenho do
		// computador.
		if ((level == 0 || this.isOver())) {
			return judge();
		}

		// Copie o jogo atual.
		tempSituation = this.clone();

		// Se o nível estiver nivelado, o computador estará tentando uma mudança.
		if (level % 2 == 0) {
			tempSituation.setWhoseTurn(COMPUTER_TURN);
		}

		// Se o nível for ímpar, o jogador estará tentando uma jogada.
		else {
			tempSituation.setWhoseTurn(PLAYER_TURN);
		}

		// Coloque a primeira jogada legal e determine o quão boa é para o computador.
		tryMove = legalMoves[0];
		tempSituation.placePiece(tempSituation.getWhoseTurn(), tryMove);
		bestGuessValue = tempSituation.bestGuess(level - 1);

		// Execute os movimentos legais restantes.
		int currentMoveIndex = 1;
		while (currentMoveIndex < legalMoves.length) {
			// Apague a jogada anterior e tente a próxima disponível.
			tempSituation = this.clone();
			tryMove = legalMoves[currentMoveIndex];
			tempSituation.placePiece(tempSituation.getWhoseTurn(), tryMove);

			currentGuessValue = tempSituation.bestGuess(level - 1);

			// Se for a vez do jogador, queremos considerar essa ação para impedi-lo de
			// vencer.
			if (tempSituation.getWhoseTurn() == PLAYER_TURN) {
				bestGuessValue = Math.max(bestGuessValue, currentGuessValue);
			}

			// Se for a vez do computador, queremos tomar a decisão que pode vencer.
			else {
				bestGuessValue = Math.min(bestGuessValue, currentGuessValue);
			}

			currentMoveIndex++;
		}

		return bestGuessValue;
	}

	/**
	 * Atualiza o jogo para que o jogador ({player} _TURN) faça um movimento no
	 * especificado ponto na grade.
	 */
	public void placePiece(int player, int move) {
		grid[move] = (player == PLAYER_TURN) ? PLAYER_MARK : COMPUTER_MARK;

		numMoves++;

		// Gere o novo conjunto de jogadas executadas/não executadas no jogo.
		generateMoves();
	}

	/**
	 * Retorna um valor inteiro com base no exame do estado do jogo: 0 - o jogo está
	 * em andamento 1 - o jogador ganhou 2 - o computaor ganhou 3 - o jogo está
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

		// Se houver 9 jogadas nesse momento, o jogo será empatado.
		if (numMoves == 9) {
			return 3;
		}

		// Caso contrário, o jogo continua!
		else {
			return 0;
		}
	}

	/**
	 * Retorna um valor inteiro que descreve a qualidade da situação atual do
	 * computador. 100: o computador ganhou 50: computador / jogador está empatado
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
	 * Retorna true se o jogo acabou ou false se não estiver.
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
	 * Retorne o valor int de quem será o primeiro neste jogo.
	 */
	public int getFirstTurn() {
		return firstTurn;
	}

	/**
	 * Define o valor de quem será o primeiro neste jogo.
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
