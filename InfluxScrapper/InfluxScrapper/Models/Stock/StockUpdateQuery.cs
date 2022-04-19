using System.ComponentModel.DataAnnotations;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper;

public class StockUpdateQuery : StockQuery
{
    public StockUpdateQuery()
    {
        
    }
    public StockUpdateQuery(StockQuery query, int? months = null)
    {
        Type = query.Type;
        Symbol = query.Symbol;
        Interval = query.Interval;
        Months = months;
    }
    
    [Range(1, 24)]
    public int? Months { get; set; }

    public IEnumerable<StockScrapeQuery> ToScrapeQueries()
    {
        if (Type != StockType.Intraday || Months is null)
            yield return new StockScrapeQuery(this);
        for (int i = 0; i < Months; i++)
            yield return new StockScrapeQuery(this, i % 12 + 1, i / 12 + 1);
    }
    
}