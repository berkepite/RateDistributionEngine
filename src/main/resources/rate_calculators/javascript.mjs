import {Decimal} from 'vendor/decimal.js/decimal.mjs';

export function calculateMean(bids, asks) {
    const [bid_mean, ask_mean] = _calculateMeans(bids, asks);

    return [bid_mean.toNumber(), ask_mean.toNumber()];
}

export function hasAtLeastOnePercentDiff(rate1_bid, rate1_ask, rate2_bid, rate2_ask) {
    let rate1_bid_dec = new Decimal(rate1_bid.toString());
    let rate1_ask_dec = new Decimal(rate1_ask.toString());
    let rate2_bid_dec = new Decimal(rate2_bid.toString());
    let rate2_ask_dec = new Decimal(rate2_ask.toString());

    const percentageAsk = rate1_ask_dec.minus(rate2_ask_dec).dividedBy(rate1_ask_dec).mul(100).abs();
    const percentageBid = rate1_bid_dec.minus(rate2_bid_dec).dividedBy(rate1_bid_dec).mul(100).abs();

    const diff = percentageAsk.plus(percentageBid).dividedBy(2);

    return diff.greaterThanOrEqualTo(1);
}

export function calculateUSDMID(bids, asks) {
    const [bid_mean, ask_mean] = _calculateMeans(bids, asks);

    const usdmid = bid_mean.plus(ask_mean).dividedBy('2');

    return usdmid.toNumber();
}

function _calculateMeans(bids, asks) {
    let bid_sum = new Decimal(0);

    for (const bid of bids) {
        bid_sum = bid_sum.plus(bid.toString());
    }

    const bid_mean = new Decimal(bid_sum.dividedBy(bids.length));

    let ask_sum = new Decimal(0);

    for (const ask of asks) {
        ask_sum = ask_sum.plus(ask.toString());
    }

    const ask_mean = new Decimal(ask_sum.dividedBy(asks.length));

    return [bid_mean, ask_mean]
}