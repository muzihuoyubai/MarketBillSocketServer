package club.banyuan.service;

import club.banyuan.entity.Provider;
import club.banyuan.exception.RequestException;
import club.banyuan.http.HttpStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderService {

  private static List<Provider> providerList = new ArrayList<>();
  private static int providerId = 1;

  public void addProvider(Provider provider) {
    provider.setId(ProviderService.providerId++);
    providerList.add(provider);
  }

  public void updateProvider(Provider provider) {
    Optional<Provider> id = providerList.stream()
        .filter(t -> t.getId() == provider.getId()).findFirst();
    if (id.isPresent()) {
      providerList.remove(id.get());
      providerList.add(provider);
      providerList.sort(Comparator.comparingInt(Provider::getId));
    } else {
      //TODO Exception
    }
  }

  public List<Provider> getProviderList() {
    return providerList;
  }

  /**
   * 根据供应商名称或描述进行过滤
   *
   * @param provider
   * @return
   */
  public List<Provider> getProviderList(Provider provider) {
    if (provider == null) {
      return getProviderList();
    }
    return providerList.stream().filter(t -> {
      String name = provider.getName();
      if (name != null && name.trim().length() > 0) {
        return t.getName().contains(name);
      } else {
        return true;
      }
    }).filter(t -> {
      String desc = provider.getDesc();
      if (desc != null && desc.trim().length() > 0) {
        return t.getDesc().contains(desc);
      } else {
        return true;
      }
    }).collect(Collectors.toList());
  }

  public Provider getProviderById(int id) {
    Optional<Provider> providerOptional = ProviderService.providerList.stream()
        .filter(t -> t.getId() == id).findFirst();
    if (providerOptional.isPresent()) {
      return providerOptional.get();
    } else {
      throw new RequestException(HttpStatus.BAD_REQUEST, "供应商" + id + "不存在");
    }
  }

  public void deleteProviderById(int id) {
    Provider providerById = getProviderById(id);
    providerList.remove(providerById);
  }
}
