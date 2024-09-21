package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=8a32db79";
    private SerieRepository serieRepository;

    // private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Adicionar série
                    2 - Listar episódios
                    3 - Listar séries adicionadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 séries
                    7 - Buscar séries por categoria
                    8 - Buscar séries pelo número de temporadas
                    9 - Buscar episódio por trecho do título
                    10 - Top 5 episódios por série
                    11 - Buscar episódios a partir de uma data

                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAutor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPeloNumeroDeTemporadas();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        
        serieRepository.save(serie);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serie.getTotalTemporadas(); i++) {
            var json = consumo
                    .obterDados(
                            ENDERECO + serie.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(d -> d.episodios().stream()
                        .map(e -> new Episodio(d.numero(), e)))
                .collect(Collectors.toList());

        serie.setEpisodios(episodios);
        serieRepository.save(serie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            System.out.println(serie.get().getEpisodios());
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();
        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()) {
            System.out.println("Dados da série: \n" + serieBusca.get());
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void buscarSeriesPorAutor() {
        System.out.println("Qual ator(a) você deseja buscar?");
        var nomeAutor = leitura.nextLine();
        System.out.println("Qual a avaliação mínima desejada?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAutor, avaliacao);
        if(seriesEncontradas.size() > 0) {
            System.out.println("Séries em que " + nomeAutor + " atuou:");
            seriesEncontradas.forEach(s ->
            System.out.println("%s; Avaliação: %.2f".formatted(s.getTitulo(), s.getAvaliacao())));

        }
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Qual a categoria desejada?");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);

        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);
        seriesPorCategoria.forEach(s -> 
        System.out.println("Série: %s; Categoria: %s".formatted(s.getTitulo(), s.getGenero())));
    }

    private void buscarSeriesPeloNumeroDeTemporadas() {
        System.out.println("Qual o número de temporadas desejado?");
        var temporadas = leitura.nextInt();
        System.out.println("Qual a avaliação mínima desejada?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesFiltradas = serieRepository.seriesPorTemporadaEAvaliacao(temporadas, avaliacao);
        seriesFiltradas.forEach(s ->
        System.out.println("Série: %s; Total de temporadas: %d; Avaliação: %.2f".formatted(s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao())));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o episódio desejado?");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trechoEpisodio);
        System.out.println("Episódios encontrados:");
        episodiosEncontrados.forEach(e -> System.out.println("Título: %s; Temporada: %d; Série: %s".formatted(e.getTitulo(), e.getTemporada(), e.getSerie())));
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()) {
            List<Episodio> topEpisodios = serieRepository.topEpisodiosPorSerie(serieBusca.get());
            topEpisodios.forEach(e ->
            System.out.println("Título do episódio: %s; Temporada: %d; Número do episódio: %d; Avaliação: %.2f".formatted(e.getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getAvaliacao())));
        }
    }

    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()) {
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = serieRepository.episodioPorSerieEAno(anoLancamento, serieBusca.get());
            episodiosAno.forEach(e ->
            System.out.println("Título: %s; Temporada: %d; Ano de lançamento: %s".formatted(e.getTitulo(), e.getTemporada(), e.getDataLancamento())));
        }
    }
}