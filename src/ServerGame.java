import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerGame {
	private JogoDaVelha game;
	private Scanner scanner;
	private BufferedReader input;
	private DataOutputStream output;

	/**
	 * Estabelece um jogo de servidor do JogoDaVelha baseado em fluxos de entrada e
	 * sa�da especificados.
	 */
	ServerGame(BufferedReader input, DataOutputStream output) {
		this.input = input;
		this.output = output;
	}

	// Estabele�a e comece um novo jogo de JogoDaVelha.
	public void start() throws InputMismatchException, CloneNotSupportedException, IOException {
		scanner = new Scanner(input);

		// Estabele�a um novo jogo de JogoDaVelha!
		game = new JogoDaVelha();
		game.chooseFirstPlayer();

		// Jogue at� o jogo terminar!
		while (!game.isOver()) {
			// Se o computador tiver o primeiro movimento...
			if (game.getFirstTurn() == game.getCOMPUTER_TURN()) {
				doComputerTurn();

				if (!game.isOver()) {
					doPlayerTurn();
				}
			}

			// Caso contr�rio, o jogador tem o primeiro movimento...
			else {
				doPlayerTurn();

				if (!game.isOver()) {
					doComputerTurn();
				}
			}
		}

		// O jogo acabou:
		// Imprime o estado final do quadro.
		// output.writeBytes(game.drawBoard());

		// Determine como o jogo terminou e alerte o usu�rio.
		switch (game.result()) {
		// Jogador venceu.
		case 1:
			System.out.println("O jogador venceu o jogo.");
			// "#P" - o jogador venceu o jogo
			output.writeBytes("#P\n");
			break;
		// Computador ganhou.
		case 2:
			System.out.println("O computador ganhou o jogo.");
			// "#C" - o computador venceu o jogo
			output.writeBytes("#C\n");
			break;
		// Jogo empatou.
		case 3:
			System.out.println("O jogo est� empatado.");
			// "#T" - o jogo est� empatado
			output.writeBytes("#T\n");
			break;
		}

		// Verifique se o usu�rio gostaria de jogar novamente.
		System.out.println("Determinando se o usu�rio gostaria de jogar outro jogo...");

		String decision = "";

		while (!decision.equals("#NG") && !decision.equals("#CG")) {
			decision = input.readLine();
		}

		// Se aplic�vel, reinicie o jogo para o usu�rio.
		if (decision.equals("#NG")) {
			System.out.println("O usu�rio gostaria de jogar outro jogo.");
			this.start();
		}

		// Caso contr�rio, apague os fluxos e volte ao servidor.
		else {
			System.out.println("O usu�rio N�O gostaria de jogar outro jogo.");
			input.close();
			output.close();
		}
	}

	// Execute a vez do jogador.
	public void doPlayerTurn() throws IOException {
		game.setWhoseTurn(game.getPLAYER_TURN());

		int userMove = -1;

		// Pe�a ao usu�rio a sua jogada at� que ele ou ela insira uma jogada v�lida.
		while (!game.legalMove(userMove)) {
			output.writeBytes(game.drawBoard());

			// Obtenha o movimento desejado do usu�rio.
			try {
				userMove = scanner.nextInt();
			}

			// Se o usu�rio digitar um valor n�oInt, defina seu movimento igual a -1 (para
			// for�ar a reinser��o).
			// Redefina o scanner para evitar loop infinito.
			catch (InputMismatchException e) {
				userMove = -1;
				scanner = new Scanner(input);
			}

			// Existem graciosamente se o usu�rio fechar a janela ou o jogo terminar de
			// repente.
			catch (NoSuchElementException e) {
				System.err.println("O jogo foi encerrado pelo usu�rio.");
			}
		}

		// Fa�a a jogada legal do usu�rio.
		game.placePiece(game.getPLAYER_TURN(), userMove);
	}

	// Execute a vez do computador.
	public void doComputerTurn() throws CloneNotSupportedException {
		game.setWhoseTurn(game.getCOMPUTER_TURN());
		game.computerMove();
	}
}