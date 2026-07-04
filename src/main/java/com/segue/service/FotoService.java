package com.segue.service;

import static java.nio.file.FileSystems.getDefault;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.SegueMe;
import com.segue.util.jsf.FacesUtil;

public class FotoService implements Serializable {

	private static final long serialVersionUID = 1L;

	private Path diretorioRaizTemp;

	@PostConstruct
	void init() {
		diretorioRaizTemp = getDefault().getPath(System.getProperty("java.io.tmpdir"), "pedidovenda-fotos-temp");
		try {
			Files.createDirectories(diretorioRaizTemp);
		} catch (IOException e) {
			throw new RuntimeException("Problemas ao tentar criar diretórios.", e);
		}
	}

	public String salvarFotoTemp(String nome, byte[] conteudo) throws IOException {
		String novoNome = renomearArquivo(nome);
		Path fotoTemp = diretorioRaizTemp.resolve(novoNome);

		Files.write(fotoTemp, conteudo);

		return novoNome;
	}

	public byte[] salvarFotoTempByte(String nome, byte[] conteudo) throws IOException {
		String novoNome = renomearArquivo(nome);
		Path fotoTemp = diretorioRaizTemp.resolve(novoNome);

		Files.write(fotoTemp, conteudo);

		return conteudo;
	}

	private String renomearArquivo(String original) {
		return UUID.randomUUID().toString() + "___" + original;
	}

	public void deletarTemp(Integer nome, String pasta) throws IOException {
		Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
				+ "/resources/temp/" + pasta + "/");
		deletar(path, nome);
	}

	private void deletar(Path raiz, Integer id) throws IOException {
		if (StringUtils.isEmpty("" + id)) {
			return;
		}
		Path foto = raiz.resolve("" + id + ".jpg");
		if (Files.exists(foto)) {
			Files.delete(foto);
		}
	}

	public void recuperarFotoTemporaria(String nome) throws IOException {
		if (StringUtils.isEmpty(nome)) {
			return;
		}

		Path fotoTemp = diretorioRaizTemp.resolve(nome);
		if (!Files.exists(fotoTemp)) {
			return;
		}
	}

	public byte[] recuperar(String nome) throws IOException {
		Path foto = this.diretorioRaizTemp.resolve(nome);
		if (Files.exists(foto)) {
			return Files.readAllBytes(foto);
		}
		throw new RuntimeException("Não encontrada imagem " + nome);
	}

	public String recuperarViaByte(byte[] conteudo, String pasta, Integer id) {
		String imagem = "";
		try {
			if (conteudo != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/" + pasta);
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(conteudo);
					fos.close();
				}
				return imagem = "../resources/temp/" + pasta + "/" + id + ".jpg";
			} else {
				return imagem = null;
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagem = null;
		}

	}
	
	public String recuperarViaBytePNG(byte[] conteudo, String pasta, Integer id) {
		String imagem = "";
		try {
			if (conteudo != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/" + pasta);
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".png");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(conteudo);
					fos.close();
				}
				return imagem = "../resources/temp/" + pasta + "/" + id + ".png";
			} else {
				return imagem = null;
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagem = null;
		}

	}

	/**
	 * Valida e normaliza a imagem enviada: decodifica o conteúdo, achata sobre
	 * fundo branco (removendo canal alfa) e reescreve como JPEG baseline RGB —
	 * formato que o JasperReports consegue renderizar nos crachás e quadrantes.
	 *
	 * Lança {@link NegocioException} quando o conteúdo não é uma imagem
	 * decodificável (ex.: arquivo não-JPEG renomeado, JPEG CMYK/progressivo que
	 * o ImageIO não consegue ler). Assim o erro aparece no momento do upload, e
	 * não silenciosamente como imagem em branco no PDF.
	 */
	public byte[] normalizarImagemJpeg(byte[] conteudo) throws NegocioException {
		if (conteudo == null) {
			return null;
		}
		try {
			BufferedImage original = ImageIO.read(new ByteArrayInputStream(conteudo));
			if (original == null) {
				throw new NegocioException(
						"Imagem inválida ou em formato não suportado. Envie um JPG comum (baseline/RGB).");
			}
			BufferedImage rgb = new BufferedImage(original.getWidth(), original.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = rgb.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
			g.drawImage(original, 0, 0, null);
			g.dispose();
			ByteArrayOutputStream saida = new ByteArrayOutputStream();
			ImageIO.write(rgb, "jpg", saida);
			return saida.toByteArray();
		} catch (IOException e) {
			throw new NegocioException("Não foi possível processar a imagem: " + e.getMessage());
		}
	}

	/**
	 * Materializa as três imagens do retiro (padroeira, fundo e rodapé) no
	 * diretório temp de onde os relatórios "com layout" as leem por caminho
	 * físico. Deve ser chamado antes de gerar o quadrante, pois os arquivos em
	 * {@code resources/temp/...} são apagados a cada redeploy/restart do WAR e
	 * hoje só eram regravados ao abrir a tela de cadastro do Segue-Me.
	 *
	 * Falhas por imagem são ignoradas (a imagem apenas fica em branco no PDF),
	 * para nunca abortar a geração do relatório.
	 */
	public void materializarImagensSegueMe(SegueMe segueMe) {
		if (segueMe == null || segueMe.getId() == null) {
			return;
		}
		Integer id = Integer.valueOf(segueMe.getId().toString());
		materializar(segueMe.getImagem(), "segueMe", id);
		materializar(segueMe.getImagemFundo(), "segueMeFundo", id);
		materializar(segueMe.getImagemRoda(), "segueMeRoda", id);
	}

	private void materializar(byte[] conteudo, String pasta, Integer id) {
		if (conteudo == null) {
			return;
		}
		try {
			byte[] normalizada = normalizarImagemJpeg(conteudo);
			Path diretorio = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/")
					+ "resources/temp/" + pasta);
			if (!Files.exists(diretorio)) {
				Files.createDirectories(diretorio);
			}
			Path arquivo = Paths.get(diretorio.toRealPath() + "/" + id + ".jpg");
			Files.write(arquivo, normalizada);
		} catch (NegocioException | IOException e) {
			// imagem inválida/ilegível: mantém em branco no relatório, sem abortar
		}
	}

	public String recuperarViaByteLog(byte[] conteudo, String pasta, Long id) {
		String imagem = "";
		try {
			if (conteudo != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/" + pasta);
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(conteudo);
					fos.close();
				}
				return imagem = "../resources/temp/" + pasta + "/" + id + ".jpg";
			} else {
				return imagem = null;
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagem = null;
		}

	}

}