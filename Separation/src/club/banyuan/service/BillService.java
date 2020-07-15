package club.banyuan.service;

import club.banyuan.entity.Bill;
import club.banyuan.entity.User;
import club.banyuan.exception.RequestException;
import club.banyuan.http.HttpStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BillService {

  private static int billId = 1;

  private static List<Bill> billList = new ArrayList<>();

  public List<Bill> getBillList() {
    return billList;
  }

  public List<Bill> getBillList(Bill bill) {
    if (bill == null) {
      return getBillList();
    }
    return billList.stream().filter(t -> {
      String product = bill.getProduct();
      if (product != null && product.trim().length() > 0) {
        return t.getProduct().contains(product);
      } else {
        return true;
      }
    }).filter(t -> {
      if (bill.getIsPay() > 0) {
        return bill.getIsPay() == t.getIsPay();
      } else {
        return true;
      }
    }).collect(Collectors.toList());

  }

  public void addBill(Bill bill) {
    bill.setUpdateTime(new Date());
    bill.setId(billId++);
    billList.add(bill);
  }

  public void updateBill(Bill bill) {
    Bill billById = getBillById(bill.getId());
    bill.setUpdateTime(new Date());
    billList.remove(billById);
    billList.add(bill);
    billList.sort(Comparator.comparingInt(Bill::getId));
  }

  public Bill getBillById(int id) {
    Optional<Bill> billOptional = billList.stream()
        .filter(t -> t.getId() == id).findFirst();
    if (billOptional.isPresent()) {
      return billOptional.get();
    } else {
      throw new RequestException(HttpStatus.BAD_REQUEST, "账单" + id + "不存在");
    }
  }

  public void deleteBilById(int id) {
    Bill bill = getBillById(id);
    billList.remove(bill);
  }
}
