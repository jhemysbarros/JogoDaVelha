import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	/**
	 * Porta na qual o servidor ser� executado.
	 */
	static int PORT = 9999;

	public static void main(String[] args) {
		// For�a o servidor a funcionar at� que ele seja encerrado.
		while (true) {
			// Monitore as conex�es com o PORT (se estiver dispon�vel).
			try {
				ServerSocket welcomeSocket = new ServerSocket(PORT);
				System.out.println("O servidor est� agora em execu��o na porta " + PORT + "...");

				// Quando welcomeSocket � contatado, ele retorna um soquete para lidar com a
				// comunica��o com o cliente.
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Um usu�rio se conectou de " + connectionSocket.getInetAddress());

				// Estabele�a o fluxo de entrada do cliente.
				BufferedReader clientInput = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream()));

				// Estabele�a o fluxo de sa�da do servidor.
				DataOutputStream serverOutput = new DataOutputStream(connectionSocket.getOutputStream());

				// Crie uma vers�o baseada em jogo do servidor do JogoDaVelha.
				ServerGame game = new ServerGame(clientInput, serverOutput);

				try {
					game.start();
				}

				catch (CloneNotSupportedException e) {
					System.err.println("O jogo falhou ao iniciar.");
					System.exit(-1);
				}

				finally {
					connectionSocket.close();
				}
			}

			// Capture todos os erros de execu��o / IO que possam ocorrer.
			catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}