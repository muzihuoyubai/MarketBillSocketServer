package club.banyuan.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.Date;

public class Bill {

  private int id;
  private double money;
  private int providerId;
  private String providerName;
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;
  private int isPay;
  private String isPayStr;
  private String product;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public double getMoney() {
    return money;
  }

  public void setMoney(double money) {
    this.money = money;
  }

  public int getProviderId() {
    return providerId;
  }

  public void setProviderId(int providerId) {
    this.providerId = providerId;
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public int getIsPay() {
    return isPay;
  }

  public void setIsPay(int isPay) {
    this.isPay = isPay;
    if (isPay == 1) {
      isPayStr = "是";
    } else if (isPay == 0) {
      isPayStr = "否";
    }
  }

  public String getIsPayStr() {
    return isPayStr;
  }

  public void setIsPayStr(String isPayStr) {
    this.isPayStr = isPayStr;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }
}
