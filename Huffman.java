import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// Nomes dos integrantes do grupo:
// 1. [Seu Nome]
// 2. [Nome do Colega]
// 3. [Nome do Colega]
// 4. [Nome do Colega]

public class Huffman {

    // Classe do no da arvore implementando Comparable
    static class No implements Comparable<No> {
        char caractere;
        int frequencia;
        No esquerda;
        No direita;

        public No(char caractere, int frequencia) {
            this.caractere = caractere;
            this.frequencia = frequencia;
            this.esquerda = null;
            this.direita = null;
        }

        public boolean ehFolha() {
            return esquerda == null && direita == null;
        }

        @Override
        public int compareTo(No outroNo) {
            return this.frequencia - outroNo.frequencia;
        }
    }

    // Min-heap usando ArrayList conforme exigido no PDF
    static class MinHeap {
        private ArrayList<No> heap;

        public MinHeap() {
            heap = new ArrayList<>();
        }

        public void inserir(No no) {
            heap.add(no);
            subir(heap.size() - 1);
        }

        public No removerMin() {
            if (heap.isEmpty()) {
                return null;
            }

            No menor = heap.get(0);
            int ultimoIndice = heap.size() - 1;
            No ultimo = heap.remove(ultimoIndice);

            if (!heap.isEmpty()) {
                heap.set(0, ultimo);
                descer(0);
            }

            return menor;
        }

        public int getTamanho() {
            return heap.size();
        }

        private void subir(int i) {
            while (i > 0) {
                int pai = (i - 1) / 2;

                if (heap.get(i).compareTo(heap.get(pai)) < 0) {
                    trocar(i, pai);
                    i = pai;
                } else {
                    break;
                }
            }
        }

        private void descer(int i) {
            int tamanho = heap.size();
            while (true) {
                int menor = i;
                int esquerda = 2 * i + 1;
                int direita = 2 * i + 2;

                if (esquerda < tamanho && heap.get(esquerda).compareTo(heap.get(menor)) < 0) {
                    menor = esquerda;
                }

                if (direita < tamanho && heap.get(direita).compareTo(heap.get(menor)) < 0) {
                    menor = direita;
                }

                if (menor != i) {
                    trocar(i, menor);
                    i = menor;
                } else {
                    break;
                }
            }
        }

        private void trocar(int i, int j) {
            No aux = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, aux);
        }

        public void imprimirHeap() {
            System.out.print("[ ");
            for (int i = 0; i < heap.size(); i++) {
                No no = heap.get(i);
                if (no.ehFolha()) {
                    System.out.print("No('" + mostrarChar(no.caractere) + "'," + no.frequencia + ")");
                } else {
                    System.out.print("No(*," + no.frequencia + ")");
                }

                if (i < heap.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(" ]");
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso correto:");
            System.out.println("Para comprimir: java -jar huffman.jar c <arquivo_original> <arquivo_comprimido>");
            System.out.println("Para descomprimir: java -jar huffman.jar d <arquivo_comprimido> <arquivo_restaurado>");
            return;
        }

        String operacao = args[0];
        String arquivoEntrada = args[1];
        String arquivoSaida = args[2];

        try {
            if (operacao.equals("c")) {
                comprimir(arquivoEntrada, arquivoSaida);
            } else if (operacao.equals("d")) {
                descomprimir(arquivoEntrada, arquivoSaida);
            } else {
                System.out.println("Operacao invalida. Use c ou d.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void comprimir(String arquivoEntrada, String arquivoSaida) throws IOException {
        byte[] dados = lerArquivo(arquivoEntrada);

        if (dados.length == 0) {
            System.out.println("Arquivo vazio. Nada para comprimir.");
            return;
        }

        int[] frequencias = contarFrequencias(dados);
        imprimirTabelaFrequencia(frequencias);

        MinHeap heap = montarHeap(frequencias);
        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 2: Min-Heap Inicial (ArrayList)");
        System.out.println("--------------------------------------------------");
        heap.imprimirHeap();

        No raiz = construirArvore(heap);

        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 3: Arvore de Huffman");
        System.out.println("--------------------------------------------------");
        imprimirArvore(raiz, 0);

        String[] codigos = new String[256];
        gerarCodigos(raiz, "", codigos);

        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 4: Tabela de Codigos de Huffman");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < 256; i++) {
            if (frequencias[i] > 0) {
                System.out.println("Caractere '" + mostrarChar((char) i) + "': " + codigos[i]);
            }
        }

        String bitsCompactados = gerarBitsCompactados(dados, codigos);
        int quantidadeBitsValidos = bitsCompactados.length();
        byte[] bytesCompactados = bitsParaBytes(bitsCompactados);

        escreverArquivoComprimido(arquivoSaida, frequencias, quantidadeBitsValidos, bytesCompactados);

        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 5: Resumo da Compressao");
        System.out.println("--------------------------------------------------");
        System.out.println("Tamanho original....: " + (dados.length * 8) + " bits (" + dados.length + " bytes)");
        System.out.println("Tamanho comprimido..: " + quantidadeBitsValidos + " bits (" + bytesCompactados.length + " bytes)");

        double taxa = (1.0 - ((double) bytesCompactados.length / (double) dados.length)) * 100.0;
        System.out.printf("Taxa de compressao..: %.2f%%\n", taxa);
        System.out.println("--------------------------------------------------");

        System.out.println("Arquivo comprimido com sucesso.");
    }

    public static void descomprimir(String arquivoEntrada, String arquivoSaida) throws IOException {
        File arquivo = new File(arquivoEntrada);
        FileInputStream fis = new FileInputStream(arquivo);

        int[] frequencias = new int[256];
        int totalCaracteres = 0; // Somatorio local em vez de vetor global!
        for (int i = 0; i < 256; i++) {
            frequencias[i] = lerInt(fis);
            totalCaracteres += frequencias[i];
        }

        int quantidadeBitsValidos = lerInt(fis);

        long bytesRestantesLong = arquivo.length() - (256L * 4L) - 4L;
        if (bytesRestantesLong < 0) {
            fis.close();
            throw new IOException("Arquivo comprimido invalido.");
        }

        int bytesRestantes = (int) bytesRestantesLong;
        byte[] bytesCompactados = new byte[bytesRestantes];

        int totalLido = 0;
        while (totalLido < bytesRestantes) {
            int lidos = fis.read(bytesCompactados, totalLido, bytesRestantes - totalLido);
            if (lidos == -1) {
                break;
            }
            totalLido += lidos;
        }

        fis.close();

        MinHeap heap = montarHeap(frequencias);
        No raiz = construirArvore(heap);

        if (raiz == null) {
            throw new IOException("Nao foi possivel reconstruir a arvore.");
        }

        byte[] dadosOriginais = decodificar(bytesCompactados, quantidadeBitsValidos, raiz, totalCaracteres);
        escreverArquivo(arquivoSaida, dadosOriginais);

        System.out.println("Arquivo descomprimido com sucesso.");
    }

    public static byte[] lerArquivo(String caminho) throws IOException {
        File arquivo = new File(caminho);
        FileInputStream fis = new FileInputStream(arquivo);

        byte[] dados = new byte[(int) arquivo.length()];
        int totalLido = 0;

        while (totalLido < dados.length) {
            int lidos = fis.read(dados, totalLido, dados.length - totalLido);
            if (lidos == -1) {
                break;
            }
            totalLido += lidos;
        }

        fis.close();
        return dados;
    }

    public static void escreverArquivo(String caminho, byte[] dados) throws IOException {
        FileOutputStream fos = new FileOutputStream(caminho);
        fos.write(dados);
        fos.close();
    }

    public static int[] contarFrequencias(byte[] dados) {
        int[] frequencias = new int[256];

        for (int i = 0; i < dados.length; i++) {
            frequencias[dados[i] & 0xFF]++;
        }

        return frequencias;
    }

    public static MinHeap montarHeap(int[] frequencias) {
        MinHeap heap = new MinHeap();

        for (int i = 0; i < 256; i++) {
            if (frequencias[i] > 0) {
                heap.inserir(new No((char) i, frequencias[i]));
            }
        }

        return heap;
    }

    public static No construirArvore(MinHeap heap) {
        if (heap.getTamanho() == 0) {
            return null;
        }

        // caso especial: so existe um caractere no arquivo
        if (heap.getTamanho() == 1) {
            No unico = heap.removerMin();
            No raiz = new No('\0', unico.frequencia);
            raiz.esquerda = unico;
            return raiz;
        }

        while (heap.getTamanho() > 1) {
            No esquerda = heap.removerMin();
            No direita = heap.removerMin();

            No pai = new No('\0', esquerda.frequencia + direita.frequencia);
            pai.esquerda = esquerda;
            pai.direita = direita;

            heap.inserir(pai);
        }

        return heap.removerMin();
    }

    public static void gerarCodigos(No no, String codigoAtual, String[] codigos) {
        if (no == null) {
            return;
        }

        if (no.ehFolha()) {
            if (codigoAtual.equals("")) {
                codigos[no.caractere] = "0";
            } else {
                codigos[no.caractere] = codigoAtual;
            }
            return;
        }

        gerarCodigos(no.esquerda, codigoAtual + "0", codigos);
        gerarCodigos(no.direita, codigoAtual + "1", codigos);
    }

    public static String gerarBitsCompactados(byte[] dados, String[] codigos) {
        // Uso de StringBuilder para performance em arquivos grandes
        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < dados.length; i++) {
            bits.append(codigos[dados[i] & 0xFF]);
        }

        return bits.toString();
    }

    public static byte[] bitsParaBytes(String bits) {
        int quantidadeBytes = (bits.length() + 7) / 8;
        byte[] resultado = new byte[quantidadeBytes];
        int indiceByte = 0;

        for (int i = 0; i < bits.length(); i += 8) {
            StringBuilder pedaco = new StringBuilder();

            for (int j = i; j < i + 8 && j < bits.length(); j++) {
                pedaco.append(bits.charAt(j));
            }

            while (pedaco.length() < 8) {
                pedaco.append("0");
            }

            resultado[indiceByte] = (byte) Integer.parseInt(pedaco.toString(), 2);
            indiceByte++;
        }

        return resultado;
    }

    public static String bytesParaBits(byte[] bytesCompactados, int quantidadeBitsValidos) {
        // Uso de StringBuilder para performance em arquivos grandes
        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < bytesCompactados.length; i++) {
            int valor = bytesCompactados[i] & 0xFF;
            String binario = Integer.toBinaryString(valor);

            while (binario.length() < 8) {
                binario = "0" + binario;
            }

            bits.append(binario);
        }

        if (quantidadeBitsValidos < bits.length()) {
            return bits.substring(0, quantidadeBitsValidos);
        }

        return bits.toString();
    }

    public static byte[] decodificar(byte[] bytesCompactados, int quantidadeBitsValidos, No raiz, int totalCaracteres) {
        // caso especial: so um caractere no arquivo
        if (raiz.ehFolha() || (raiz.esquerda != null && raiz.direita == null)) {
            No folha;
            if (raiz.ehFolha()) {
                folha = raiz;
            } else {
                folha = raiz.esquerda;
            }

            byte[] resultado = new byte[folha.frequencia];
            for (int i = 0; i < resultado.length; i++) {
                resultado[i] = (byte) folha.caractere;
            }
            return resultado;
        }

        String bits = bytesParaBits(bytesCompactados, quantidadeBitsValidos);

        byte[] resultado = new byte[totalCaracteres];
        int indiceResultado = 0;

        No atual = raiz;

        // Adicionada verificacao de indiceResultado para seguranca
        for (int i = 0; i < bits.length() && indiceResultado < totalCaracteres; i++) {
            if (bits.charAt(i) == '0') {
                atual = atual.esquerda;
            } else {
                atual = atual.direita;
            }

            if (atual.ehFolha()) {
                resultado[indiceResultado] = (byte) atual.caractere;
                indiceResultado++;
                atual = raiz;
            }
        }

        return resultado;
    }

    public static void escreverArquivoComprimido(String caminho, int[] frequencias, int quantidadeBitsValidos, byte[] dadosCompactados) throws IOException {
        FileOutputStream fos = new FileOutputStream(caminho);

        // salva as 256 frequencias
        for (int i = 0; i < 256; i++) {
            escreverInt(fos, frequencias[i]);
        }

        // salva quantidade de bits validos
        escreverInt(fos, quantidadeBitsValidos);

        // salva os dados comprimidos
        fos.write(dadosCompactados);

        fos.close();
    }

    public static void escreverInt(FileOutputStream fos, int valor) throws IOException {
        fos.write((valor >>> 24) & 0xFF);
        fos.write((valor >>> 16) & 0xFF);
        fos.write((valor >>> 8) & 0xFF);
        fos.write(valor & 0xFF);
    }

    public static int lerInt(FileInputStream fis) throws IOException {
        int b1 = fis.read();
        int b2 = fis.read();
        int b3 = fis.read();
        int b4 = fis.read();

        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
            throw new IOException("Erro ao ler inteiro do arquivo.");
        }

        return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
    }

    public static void imprimirTabelaFrequencia(int[] frequencias) {
        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 1: Tabela de Frequencia de Caracteres");
        System.out.println("--------------------------------------------------");

        for (int i = 0; i < 256; i++) {
            if (frequencias[i] > 0) {
                System.out.println("Caractere '" + mostrarChar((char) i) + "' (ASCII: " + i + "): " + frequencias[i]);
            }
        }
    }

    public static void imprimirArvore(No no, int nivel) {
        if (no == null) {
            return;
        }

        for (int i = 0; i < nivel; i++) {
            System.out.print("  ");
        }

        if (no.ehFolha()) {
            System.out.println("- ('" + mostrarChar(no.caractere) + "', " + no.frequencia + ")");
        } else if (nivel == 0) {
            System.out.println("- (RAIZ, " + no.frequencia + ")");
        } else {
            System.out.println("- (*, " + no.frequencia + ")");
        }

        imprimirArvore(no.esquerda, nivel + 1);
        imprimirArvore(no.direita, nivel + 1);
    }

    public static String mostrarChar(char c) {
        if (c == '\n') return "\\n";
        if (c == '\r') return "\\r";
        if (c == '\t') return "\\t";
        if (c == ' ') return "espaco";
        return String.valueOf(c);
    }
}