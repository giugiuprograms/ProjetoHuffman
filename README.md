# Projeto 1 - Compressão de Arquivos com o Algoritmo de Huffman

Projeto desenvolvido para a disciplina de Estrutura de Dados II. O objetivo deste projeto é implementar o clássico algoritmo de compressão sem perdas de Huffman na linguagem Java, utilizando estruturas de dados como Fila de Prioridades (Min-Heap) e Árvore Binária.

## Integrantes do Grupo
* Diego Teruya RA:10723404
* Gabriel Rodrigues RA:10409071
* Giulia Araki RA:10408954
* Leonardo Soto RA:10729329

## Tecnologias Utilizadas
* **Linguagem:** Java
* **Estruturas:** Min-Heap (via ArrayList) e Árvore Binária de Huffman.

## Como Executar o Projeto

Certifique-se de ter o Java (JDK) instalado na sua máquina.

### Compilar e Empacotar
Abra o terminal na pasta do projeto e execute os seguintes comandos para compilar as classes e gerar o arquivo executável `.jar`:

* javac Huffman.java
* jar cfe huffman.jar Huffman *.class

---
## Como Rodar no Terminal do MAC IOS

### 1. Entrar na pasta do projeto
* cd ProjetoHuffman

### 2. Compilar o código
**Compile o arquivo código-fonte Java para gerar as classes executáveis (.class):**
* javac Huffman.java

### 3. Gerar o pacote executável (.jar)
**O programa foi construído para funcionar via linha de comando através de um pacote .jar**
* jar cfe huffman.jar Huffman *.class

### 4. Testar a Compressão
**Vamos usar o algoritmo para comprimir o arquivo arq_de_teste.txt que já vem na pasta:**
* java -jar huffman.jar c arq_de_teste.txt arquivo_comprimido.huff

### 5. Testar a Descompressão
**Para comprovar que a compressão ocorreu sem perdas, vamos restaurar o arquivo à sua forma original:**
* java -jar huffman.jar d arquivo_comprimido.huff arquivo_restaurado.txt
