import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;
import java.net.*;

public class JogoDaVelhaGUI {
	private final String SERVER_IP = "127.0.0.1";
	private final int SERVER_PORT = 9999;

	private final String FREE_ICON = "resources/marvelVSdc.png";
	private final String PLAYER_ICON = "resources/ironMan.png";
	private final String COMPUTER_ICON = "resources/flash.png";

	private int winCount = 0, lossCount = 0, tieCount = 0;

	private JFrame frame;
	private Container content;
	private JPanel buttonPanel, optionsPanel;
	private JButton[] buttons;

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	// Manipula um clique em um bot�o de grade habilitado.
	private ActionListener gridClickListener = new ActionListener() {
		// Transmite a grade desejada do usu�rio para o servidor.
		public void actionPerformed(ActionEvent actionEvent) {
			String buttonNumber = actionEvent.getActionCommand();
			System.out.println("Enviando para o servidor: " + buttonNumber);
			out.println(buttonNumber + "\n");

			// Processe todos os comandos enviados pelo servidor (que deve ser uma cadeia de
			// caracteres gridStatus).
			processServerCommands();
		}
	};

	// Lida com um clique em novo jogo/fechar jogo.
	private ActionListener optionsClickListener = new ActionListener() {
		public void actionPerformed(ActionEvent actionEvent) {
			String buttonCommand = actionEvent.getActionCommand();

			// Se o usu�rio deseja fechar o jogo, envie o comando
			// string para o servidor e feche a GUI.
			if (buttonCommand.equals("close")) {
				try {
					out.println("#CG\n");
					out.close();
					in.close();
					socket.close();
				}

				catch (IOException e) {
					System.err.println("Erro ao desconectar do servidor TTT.");
				}

				System.exit(0);
			}

			// Caso contr�rio, o usu�rio deseja um novo jogo.
			// Envie a nova sequ�ncia de jogos para o servidor e atualize a grade de bot�es.
			else {
				showGrid();
				out.println("#NG\n");
				processServerCommands();
			}
		}
	};

	/**
	 * Estabele�e as conex�es necess�rias do servidor. Execute o jogo para o
	 * usu�rio.
	 * 
	 * @throws InterruptedException
	 */
	public void run() throws InterruptedException {
		// Conecte-se ao servidor JogoDaVelha.
		connectServer();

		// Carrega a GUI do jogo.
		showGUI();

		// Permite que o usu�rio jogue o jogo.
		processServerCommands();
	}

	/**
	 * Crie uma conex�o com o servidor TTT definido nas configura��es. Se for
	 * bem-sucedido, crie fluxos de entrada e sa�da para ler e gravar no servidor.
	 */
	private void connectServer() {
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		catch (UnknownHostException e) {
			System.err.println("N�o foi poss�vel encontrar o host do servidor especificado.");
			System.exit(-1);
		}

		catch (IOException e) {
			System.err.println("Ocorreu um erro de E/S. Verifique se o servidor TTT est� em execu��o.");
			System.exit(-1);
		}

		System.out.println("A GUI se conectou ao servidor.");
	}

	/**
	 * Processa todos os comandos emitidos pelo servidor.
	 *
	 * poss�veis comandos: um gridStatus de 9 caracteres - consulte updateGrid () =>
	 * espera que o cliente retorne um n�mero inteiro de 0 a 8 representando a
	 * movimenta��o do usu�rio ----- "#T" - o jogo est� empatado "#P" - o jogador
	 * venceu o jogo "#C" - o computador venceu o jogo => espera que "#CG" termine o
	 * jogo ou "#NG" para criar um novo jogo
	 *
	 */
	private void processServerCommands() {
		try {
			System.out.println("Processando o comando do servidor...");

			String serverCommand;
			serverCommand = in.readLine();
			System.err.println("Mensagem do servidor: " + serverCommand);

			// Processe qualquer sequ�ncia gridStatus.
			if (serverCommand.charAt(0) != '#') {
				updateGrid(serverCommand);
			}

			// O jogo terminou. Mostrar ao usu�rio suas estat�sticas.
			else {
				showOptions();

				// Alerte o usu�rio para o seu resultado.
				String title, text;

				if (serverCommand.equals("#T")) {
					tieCount++;
					title = "Marvel e DC est�o empatadas!";
					text = "Esta batalha terminou em um empate.";
				}

				else if (serverCommand.equals("#P")) {
					winCount++;
					title = "A Marvel triunfa!";
					text = "Voc� venceu esta batalha.";
				}

				else {
					lossCount++;
					title = "A DC triunfa!";
					text = "Voc� perdeu esta batalha.";
				}

				text += "\nVitorias: " + winCount + ", Empates: " + tieCount + ", Derrotas: " + lossCount;

				JOptionPane.showMessageDialog(null, text, title, JOptionPane.PLAIN_MESSAGE);
			}

			System.out.println("==> O controle retornou ao usu�rio.");
		}

		catch (IOException e) {
			System.err.println("Erro ao ler comandos do servidor.");
		}
	}

	/**
	 * Mostra ao usu�rio a GUI do jogo.
	 * 
	 * @throws InterruptedException
	 */
	private void showGUI() throws InterruptedException {
		showGrid();
		frame.setVisible(true);
	}

	/**
	 * Atualiza a GUI para mostrar a tela "fim do jogo" com op��es para fechar o
	 * jogo ou iniciar um novo.
	 */
	private void showOptions() {
		content.remove(buttonPanel);
		content.add(optionsPanel);
		optionsPanel.setVisible(true);
		optionsPanel.updateUI();

	}

	/**
	 * Atualiza a GUI para mostrar a grade de bot�es (e ocultar o optionsPanel, se
	 * estiver na tela).
	 */
	private void showGrid() {
		content.remove(optionsPanel);
		content.add(buttonPanel);
		buttonPanel.setVisible(true);
		buttonPanel.updateUI();
	}

	/**
	 * Com base na cadeia gridState, modifique a grade dos bot�es a ser ativada ou
	 * desativada e exiba as imagens apropriadas para o player e o computador.
	 *
	 * @param gridState gridState is a string with length 9 (representing grid[0] to
	 *                  grid[8]) with each character representing each box of the
	 *                  grid. player: "1" computer: "2" free space: "-"
	 */
	private void updateGrid(String gridState) {
		for (int i = 0; i < 9; i++) {
			JButton button = buttons[i];
			char state = gridState.charAt(i);

			// O bot�o � um espa�o livre.
			if (state == '-') {
				button.setEnabled(true);
			}

			// O bot�o est� ativado. Determine como marcar a grade (player ou computador).
			else {
				String icon;
				if (state == '1')
					icon = PLAYER_ICON;
				else
					icon = COMPUTER_ICON;

				button.setDisabledIcon(new ImageIcon(getClass().getResource(icon)));
				button.setEnabled(false);
			}
		}
	}

	/**
	 * Cont�m nosso principal objeto de quadro da GUI e seus componentes.
	 */
	JogoDaVelhaGUI() {
		// O quadro prende tudo mais na GUI.
		frame = new JFrame("Marvel vs DC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);

		/**
		 * Defina os pain�is usados nesta GUI.
		 */

		// Painel segurando os v�rios bot�es que representam a grade do jogo da velha.
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 3));

		buttons = new JButton[9];
		for (int i = 0; i < buttons.length; i++) {
			JButton button = new JButton(new ImageIcon(getClass().getResource(FREE_ICON)));
			button.setActionCommand(i + "");
			button.addActionListener(gridClickListener);

			buttons[i] = button;
			buttonPanel.add(button);
		}

		// Painel mostrando bot�es que permitem ao usu�rio iniciar / recusar novo jogo.
		optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridLayout(2, 1));

		JButton newGameButton = new JButton("Novo jogo");
		newGameButton.setActionCommand("new");
		newGameButton.addActionListener(optionsClickListener);

		JButton closeGameButton = new JButton("Sair do jogo");
		closeGameButton.setActionCommand("close");
		closeGameButton.addActionListener(optionsClickListener);

		optionsPanel.add(newGameButton);
		optionsPanel.add(closeGameButton);
		optionsPanel.setVisible(false);

		// Prepare o painel de conte�do do quadro.
		content = frame.getContentPane();
	}
}