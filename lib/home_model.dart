import 'dart:async';

import 'contact.dart';
import 'home_contract.dart';

class HomeModel implements Model {
  @override
  Future<PermissionState> canGetContacts() async {
    // TODO
    return new Future.value(PermissionState.DENIED);
  }

  @override
  Future<List<Contact>> getContactsWithMobilePhoneNumber() async {
    // TODO
    return new Future.value(null);
  }
}
