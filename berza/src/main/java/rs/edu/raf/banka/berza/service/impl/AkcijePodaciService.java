package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverview;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesReadRequest;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.service.remote.AlphaVantageService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;
import rs.edu.raf.banka.berza.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

@Service
public class AkcijePodaciService {

    private final InfluxScrapperService influxScrapperService;
    private final AlphaVantageService alphaVantageService;

    private BerzaRepository berzaRepository;
    private AkcijeRepository akcijeRepository;

    private final List<String> odabraneAkcije = Arrays.asList("AAPL", "MSFT", "GOOG", "BA", "AXP");

    @Autowired
    public AkcijePodaciService(AkcijeRepository akcijeRepository, BerzaRepository berzaRepository,
                               InfluxScrapperService influxScrapperService,
                               AlphaVantageService alphaVantageService){
        this.akcijeRepository = akcijeRepository;
        this.berzaRepository = berzaRepository;
        this.influxScrapperService = influxScrapperService;
        this.alphaVantageService = alphaVantageService;
    }

    public List<AkcijePodaciDto> getOdabraneAkcije() {
        ArrayList<AkcijePodaciDto> dtos = new ArrayList<>();
        for(String akcija: odabraneAkcije) {
            AkcijePodaciDto dto = getAkcijaByTicker(akcija);
            dtos.add(dto);
        }

        return dtos;
    }

    public AkcijePodaciDto getAkcijaByTicker(String ticker) {
        Akcije akcija = akcijeRepository.findAkcijeByOznakaHartije(ticker);
        if(akcija == null || DateUtils.isDateInDecayDays(akcija.getLastUpdated(), 1)) {
            CompanyOverview co = alphaVantageService.getCompanyOverview(ticker);
            if(co == null) {
                return null;
            }
            if(akcija == null) {
                akcija = new Akcije();
            }

            Berza berza = berzaRepository.findBerzaByOznakaBerze(co.getExchange());
            akcija.setBerza(berza);
            akcija.setOznakaHartije(co.getSymbol());
            akcija.setOpisHartije(co.getName());
            akcija.setLastUpdated(new Date());
            akcija.setOutstandingShares(co.getSharesOutstanding());
            akcija.setCena(0.0);
            akcija.setAsk(0.0);
            akcija.setBid(0.0);
            akcija.setVolume(0L);
            akcija.setPromenaIznos(0.0);

            akcijeRepository.save(akcija);
        }

        List<String> symbols = Arrays.asList(ticker);
        List<AkcijePodaciDto> dtoList = influxScrapperService.getStocksQuote(symbols);
        if(dtoList == null || dtoList.size() == 0) {
            return null;
        }

        AkcijePodaciDto dto = dtoList.get(dtoList.size()-1);
        if(akcija.getBerza() != null)
            dto.setBerzaId(akcija.getBerza().getId());
        else
            dto.setBerzaId(-1L);
        dto.setTicker(ticker);
        dto.setId(akcija.getId());
        dto.setOpisHartije(akcija.getOpisHartije());
        dto.setOutstandingShares(akcija.getOutstandingShares());

        return dto;
    }

    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now().plusDays(2);
    }

    public List<AkcijeTimeseriesDto> getAkcijeTimeseries(AkcijeTimeseriesUpdateRequest req) {
        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // TODO: Ispraviti ovo.
        // Ovo radimo zato sto AlphaVantage API baguje i nema uvek najsvezije podatke.
        // Npr. desilo se da nemaju podatke za ceo jedan dan iako je taj dan berza vec zatvorena.

        // TODO: Izdvojiti dobijanje trenutnog vremena u posebnu metodu da bi moglo da se testira
        ZonedDateTime zonedDateTime = getZonedDateTime();
        String endDate = zonedDateTime.format(endFormatter);

        if(req.getType().equals("intraday") && req.getInterval().equals("5min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    break;
                case MONDAY:
                    if (zonedDateTime.getHour() < 16) {
                        zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    }
                    break;
                default:
                    // Ovo radimo zato sto AlphaVantage API baguje i nema uvek najsvezije podatke.
                    // Npr. desilo se da nemaju podatke za ceo jedan dan iako je taj dan berza vec zatvorena.
                    zonedDateTime = ZonedDateTime.now().minusDays(2);
            }
        } else if(req.getType().equals("intraday") && req.getInterval().equals("30min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    break;
                case MONDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                    break;
                default:
                    zonedDateTime = zonedDateTime.minusDays(7); // 7 zbog vikenda
            }
        } else {
            switch (req.getRequestType()) {
                case "1m":
                    zonedDateTime = zonedDateTime.minusMonths(1);
                    break;
                case "6m":
                    zonedDateTime = zonedDateTime.minusMonths(6);
                    break;
                case "1y":
                    zonedDateTime = zonedDateTime.minusMonths(12);
                    break;
                case "2y":
                    zonedDateTime = zonedDateTime.minusMonths(24);
                    break;
                case "ytd":
                    zonedDateTime = zonedDateTime.with(firstDayOfYear());
                    break;
            }
        }

        String startDate = zonedDateTime.format(startFormatter);

        AkcijeTimeseriesReadRequest readReq = new AkcijeTimeseriesReadRequest();
        readReq.setType(req.getType());
        readReq.setSymbol(req.getSymbol());
        readReq.setInterval(req.getInterval());
        readReq.setTimeFrom(startDate);
        readReq.setTimeTo(endDate);

        return influxScrapperService.getStocksTimeseries(readReq);
    }

    public Page<Akcije> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        Akcije akcije = new Akcije();
        akcije.setOznakaHartije(oznakaHartije);
        akcije.setOpisHartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznakaHartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opisHartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Akcije> example = Example.of(akcije, exampleMatcher);

        return akcijeRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<Akcije> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<Akcije> akcije = akcijeRepository.filterAkcije(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<Akcije>(akcije, PageRequest.of(page, size), akcije.size());
    }

    public Akcije getByID(Long id){
        return akcijeRepository.findAkcijeById(id);
    }
}
