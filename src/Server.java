import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	/**
	 * Porta na qual o servidor será executado.
	 */
	static int PORT = 9999;

	public static void main(String[] args) {
		// Força o servidor a funcionar até que ele seja encerrado.
		while (true) {
			// Monitore as conexões com o PORT (se estiver disponível).
			try {
				ServerSocket welcomeSocket = new ServerSocket(PORT);
				System.out.println("O servidor está agora em execução na porta " + PORT + "...");

				// Quando welcomeSocket é contatado, ele retorna um soquete para lidar com a
				// comunicação com o cliente.
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Um usuário se conectou de " + connectionSocket.getInetAddress());

				// Estabeleça o fluxo de entrada do cliente.
				BufferedReader clientInput = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream()));

				// Estabeleça o fluxo de saída do servidor.
				DataOutputStream serverOutput = new DataOutputStream(connectionSocket.getOutputStream());

				// Crie uma versão baseada em jogo do servidor do JogoDaVelha.
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

			// Capture todos os erros de execução / IO que possam ocorrer.
			catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}