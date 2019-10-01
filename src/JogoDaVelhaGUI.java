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

	// Manipula um clique em um botão de grade habilitado.
	private ActionListener gridClickListener = new ActionListener() {
		// Transmite a grade desejada do usuário para o servidor.
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

			// Se o usuário deseja fechar o jogo, envie o comando
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

			// Caso contrário, o usuário deseja um novo jogo.
			// Envie a nova sequência de jogos para o servidor e atualize a grade de botões.
			else {
				showGrid();
				out.println("#NG\n");
				processServerCommands();
			}
		}
	};

	/**
	 * Estabeleçe as conexões necessárias do servidor. Execute o jogo para o
	 * usuário.
	 * 
	 * @throws InterruptedException
	 */
	public void run() throws InterruptedException {
		// Conecte-se ao servidor JogoDaVelha.
		connectServer();

		// Carrega a GUI do jogo.
		showGUI();

		// Permite que o usuário jogue o jogo.
		processServerCommands();
	}

	/**
	 * Crie uma conexão com o servidor TTT definido nas configurações. Se for
	 * bem-sucedido, crie fluxos de entrada e saída para ler e gravar no servidor.
	 */
	private void connectServer() {
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		catch (UnknownHostException e) {
			System.err.println("Não foi possível encontrar o host do servidor especificado.");
			System.exit(-1);
		}

		catch (IOException e) {
			System.err.println("Ocorreu um erro de E/S. Verifique se o servidor TTT está em execução.");
			System.exit(-1);
		}

		System.out.println("A GUI se conectou ao servidor.");
	}

	/**
	 * Processa todos os comandos emitidos pelo servidor.
	 *
	 * possíveis comandos: um gridStatus de 9 caracteres - consulte updateGrid () =>
	 * espera que o cliente retorne um número inteiro de 0 a 8 representando a
	 * movimentação do usuário ----- "#T" - o jogo está empatado "#P" - o jogador
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

			// Processe qualquer sequência gridStatus.
			if (serverCommand.charAt(0) != '#') {
				updateGrid(serverCommand);
			}

			// O jogo terminou. Mostrar ao usuário suas estatísticas.
			else {
				showOptions();

				// Alerte o usuário para o seu resultado.
				String title, text;

				if (serverCommand.equals("#T")) {
					tieCount++;
					title = "Marvel e DC estão empatadas!";
					text = "Esta batalha terminou em um empate.";
				}

				else if (serverCommand.equals("#P")) {
					winCount++;
					title = "A Marvel triunfa!";
					text = "Você venceu esta batalha.";
				}

				else {
					lossCount++;
					title = "A DC triunfa!";
					text = "Você perdeu esta batalha.";
				}

				text += "\nVitorias: " + winCount + ", Empates: " + tieCount + ", Derrotas: " + lossCount;

				JOptionPane.showMessageDialog(null, text, title, JOptionPane.PLAIN_MESSAGE);
			}

			System.out.println("==> O controle retornou ao usuário.");
		}

		catch (IOException e) {
			System.err.println("Erro ao ler comandos do servidor.");
		}
	}

	/**
	 * Mostra ao usuário a GUI do jogo.
	 * 
	 * @throws InterruptedException
	 */
	private void showGUI() throws InterruptedException {
		showGrid();
		frame.setVisible(true);
	}

	/**
	 * Atualiza a GUI para mostrar a tela "fim do jogo" com opções para fechar o
	 * jogo ou iniciar um novo.
	 */
	private void showOptions() {
		content.remove(buttonPanel);
		content.add(optionsPanel);
		optionsPanel.setVisible(true);
		optionsPanel.updateUI();

	}

	/**
	 * Atualiza a GUI para mostrar a grade de botões (e ocultar o optionsPanel, se
	 * estiver na tela).
	 */
	private void showGrid() {
		content.remove(optionsPanel);
		content.add(buttonPanel);
		buttonPanel.setVisible(true);
		buttonPanel.updateUI();
	}

	/**
	 * Com base na cadeia gridState, modifique a grade dos botões a ser ativada ou
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

			// O botão é um espaço livre.
			if (state == '-') {
				button.setEnabled(true);
			}

			// O botão está ativado. Determine como marcar a grade (player ou computador).
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
	 * Contém nosso principal objeto de quadro da GUI e seus componentes.
	 */
	JogoDaVelhaGUI() {
		// O quadro prende tudo mais na GUI.
		frame = new JFrame("Marvel vs DC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);

		/**
		 * Defina os painéis usados nesta GUI.
		 */

		// Painel segurando os vários botões que representam a grade do jogo da velha.
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

		// Painel mostrando botões que permitem ao usuário iniciar / recusar novo jogo.
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

		// Prepare o painel de conteúdo do quadro.
		content = frame.getContentPane();
	}
}