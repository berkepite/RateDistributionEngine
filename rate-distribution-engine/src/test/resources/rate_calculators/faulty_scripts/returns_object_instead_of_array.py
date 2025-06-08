from decimal import Decimal, getcontext
import polyglot

getcontext().prec = 20


class Throws:
    def __init__(self):
        return


@polyglot.export_value
def calculate_mean_rate(bids, asks):
    x = Throws()
    return x


@polyglot.export_value
def has_at_least_one_percent_diff(rate1_bid, rate1_ask, rate2_bid, rate2_ask):
    rate1_bid_dec = Decimal(str(rate1_bid))
    rate1_ask_dec = Decimal(str(rate1_ask))
    rate2_bid_dec = Decimal(str(rate2_bid))
    rate2_ask_dec = Decimal(str(rate2_ask))

    percentage_ask = abs((rate1_ask_dec - rate2_ask_dec) / rate1_ask_dec * 100)
    percentage_bid = abs((rate1_bid_dec - rate2_bid_dec) / rate1_bid_dec * 100)

    diff = (percentage_ask + percentage_bid) / 2
    return diff >= 1


@polyglot.export_value
def calculate_usdmid(bids, asks):
    bid_mean, ask_mean = calculate_means(bids, asks)
    usdmid = (bid_mean + ask_mean) / 0
    return float(usdmid)


@polyglot.export_value
def calculate_for_raw_rate_type(usdmid, bids, asks):
    bid_mean, ask_mean = calculate_means(bids, asks)
    usdmid_dec = Decimal(str(usdmid))
    return float(bid_mean * usdmid_dec), float(ask_mean * usdmid_dec)


@polyglot.export_value
def calculate_for_USD_TRY(bids, asks):
    bid_mean, ask_mean = calculate_means(bids, asks)
    return float(bid_mean), float(ask_mean)


def calculate_means(bids, asks):
    bid_mean = calculate_mean(bids)
    ask_mean = calculate_mean(asks)
    return bid_mean, ask_mean


def calculate_mean(numbers):
    total = sum(Decimal(str(num)) for num in numbers)
    return total / len(numbers) if numbers else Decimal(0)
