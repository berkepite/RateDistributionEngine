import {Decimal} from './vendor/decimal.js/decimal.mjs';

// export function calculateMeanRate(bids, asks) {
//     const [bid_mean, ask_mean] = calculateMeans(bids, asks);
//
//     return [bid_mean.toNumber(), ask_mean.toNumber()];
// }

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
    const [bid_mean, ask_mean] = calculateMeans(bids, asks);

    const usdmid = bid_mean.plus(ask_mean).dividedBy('2');

    return usdmid.toNumber();
}

export function calculateForRawRateType(usdmid, bids, asks) {
    const [bid_mean, ask_mean] = calculateMeans(bids, asks);

    const usdmid_dec = new Decimal(usdmid.toString());

    return [bid_mean.mul(usdmid_dec).toNumber(), ask_mean.mul(usdmid_dec).toNumber()];
}

export function calculateForUSD_TRY(bids, asks) {
    const [bid_mean, ask_mean] = calculateMeans(bids, asks);

    return [bid_mean.toNumber(), ask_mean.toNumber()];
}

function calculateMean(numbers) {
    let sum = new Decimal(0);

    for (const num of numbers) {
        sum = sum.plus(new Decimal(num.toString()));
    }

    return sum.dividedBy(numbers.length);
}

function calculateMeans(bids, asks) {
    const bid_mean = calculateMean(bids);
    const ask_mean = calculateMean(asks);

    return [bid_mean, ask_mean];
}
