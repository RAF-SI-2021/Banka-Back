using InfluxDB.Client.Core.Exceptions;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Exchange;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilities;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/forex/exchangerate")]
public class ForexExchangeInfluxScrapperController : InfluxScrapperController<ForexExchangeRateCacheQuery, ForexExchangeRateQuery,
    ForexExchangeRateCacheQuery,
    ForexExchangeRateResult>
{
    public ForexExchangeInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<ForexExchangeInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    public override IEnumerable<ForexExchangeRateQuery> ConvertToScrapeQueriesInternal(
        ForexExchangeRateCacheQuery updateQuery)
        => updateQuery.ToQuotes();

    public override ForexExchangeRateCacheQuery ConvertToUpdateQueryInternal(ForexExchangeRateCacheQuery readQuery, DateTime? lastFound)
        => readQuery;

    public override async Task<IEnumerable<ForexExchangeRateResult>> ScrapeInternal(
        ForexExchangeRateQuery scrapeQuery,
        CancellationToken token)
    {
        var resultJson = await HttpUtilities.GetJSON<ForexExchangeRateJson>(scrapeQuery.Url, _httpClientFactory, token);
        if (resultJson is null)
            throw new NullReferenceException("HTTP failed");
        var result = new ForexExchangeRateResult();
        result.Ask = resultJson.Body.Ask;
        result.Bid = resultJson.Body.Bid;
        result.ExchangeRate = resultJson.Body.ExchangeRate;
        result.FromCurrency = scrapeQuery.FromCurrency;
        result.ToCurrency = scrapeQuery.ToCurrency;
        return new[] {result};
    }
}