package cn.xgpjun.cypay.order;

public enum OrderType {
    WX("wxpay"),
    ZFB("alipay"),
    QQ("qqpay");
    final String type;

    OrderType(String type) {
        this.type = type;
    }
}
