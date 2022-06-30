package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FuturesUgovoriPodaciServiceTest {

    @Spy
    @InjectMocks
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Mock
    InfluxScrapperService influxScrapperService;

    @Test
    void testGetOdabraniFuturesUgovori() {
        FuturesUgovori future = new FuturesUgovori();
        future.setId(1L);
        FuturesPodaciDto dto = new FuturesPodaciDto();
        List<FuturesPodaciDto> res = new ArrayList<>();
        res.add(dto);
        when(futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(any())).thenReturn(future);
        when(influxScrapperService.getFuturesQoute(any())).thenReturn(res);
        assertEquals(1L, futuresUgovoriPodaciService.getOdabraniFuturesUgovori().get(0).getId());
    }


    @Test
    void testGetOdabraniFuturesUgovoriResNull() {
        FuturesUgovori future = new FuturesUgovori();
        future.setId(1L);
        when(futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(any())).thenReturn(future);
        when(influxScrapperService.getFuturesQoute(any())).thenReturn(null);
        assertNull(futuresUgovoriPodaciService.getOdabraniFuturesUgovori().get(0));
    }

    @Test
    void testIsRelevantNull(){
        when(futuresUgovoriRepository.findFuturesUgovoriByIdAndSettlementDateAfter(any(), any())).thenReturn(null);
        assertFalse(futuresUgovoriPodaciService.isRelevant(1L));
    }

    @Test
    void testIsRelevant(){
        when(futuresUgovoriRepository.findFuturesUgovoriByIdAndSettlementDateAfter(any(), any())).thenReturn(new FuturesUgovori());
        assertTrue(futuresUgovoriPodaciService.isRelevant(1L));
    }

    @Test
    void testGetFuturesUgovorById(){
        FuturesUgovori future = new FuturesUgovori();
        future.setOznakaHartije("symbol");
        when(futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(any())).thenReturn(null);
        when(futuresUgovoriRepository.findFuturesById(any())).thenReturn(future);
        when(influxScrapperService.getFuturesQoute(any())).thenReturn(null);
        assertNull(futuresUgovoriPodaciService.getFuturesUgovorById(1L));
    }

}
