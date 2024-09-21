package br.com.alura.screenmatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    public List<SerieDTO> obterTodasAsSeries() {
        return converteDados(serieRepository.findAll()) ;
    }

    public List<SerieDTO> obterTop5Series() {
        return converteDados(serieRepository.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDTO> obterLancamentos() {
       return converteDados(serieRepository.findTop5ByOrderByEpisodiosDataLancamento());
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> s = serieRepository.findById(id);
        
        if(s.isPresent()) {
            return new SerieDTO(s.get().getId(), s.get().getTitulo(), s.get().getTotalTemporadas(), s.get().getAvaliacao(),
            s.get().getGenero(), s.get().getAtores(), s.get().getPoster(), s.get().getSinopse());
        } 
        
        return null;
    }

    public List<EpisodioDTO> obterTodasAsTemporadas(Long id) {
        Optional<Serie> s = serieRepository.findById(id);
        
        if(s.isPresent()) {
            return s.get().getEpisodios()
            .stream()
            .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
            .collect(Collectors.toList());
        } 
        
        return null;
    }

    public List<EpisodioDTO> obterTemporadasPorNumero(Long id, Long numero) {
        return serieRepository.obterEpisodiosPorTemporada(id, numero)
        .stream()
        .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
        .collect(Collectors.toList());
    }

    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        return converteDados(serieRepository.findByGenero(categoria));
    }

    public List<EpisodioDTO> obterTop5EpisodiosPorSerie(Long id) {
        Optional<Serie> serie = serieRepository.findById(id);
        
        if(serie.isPresent()) {
            return serieRepository.obterTop5EpisodiosPorSerie(serie.get())
            .stream()
            .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
            .collect(Collectors.toList());
        }

        return null;
    }

    private List<SerieDTO> converteDados(List<Serie> series) {
        return series
        .stream()
        .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(),
                s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
        .collect(Collectors.toList());
    }


}
