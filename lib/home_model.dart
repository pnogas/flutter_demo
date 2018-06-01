import 'dart:async';

import 'package:flutter/services.dart';

import 'contact.dart';
import 'home_contract.dart';

class HomeModel implements Model {
  static const _methodChannel = const MethodChannel(
      'runtimepermissiontutorial/contacts');

  @override
  Future<PermissionState> canGetContacts() async {
    try {
      final int result = await _methodChannel.invokeMethod('hasPermission');
      return new Future.value(PermissionState.values.elementAt(result));
    } on PlatformException catch (e) {
      print('Exception ' + e.toString());
    }
    return new Future.value(PermissionState.DENIED);
  }

  @override
  Future<List<Contact>> getContactsWithMobilePhoneNumber() async {
    List<Contact> contacts = new List<Contact>();
    try {
      final List<dynamic> result = await _methodChannel.invokeMethod(
          'getContacts');
      if (result != null) {
        for (var contact in result) {
          contacts.add(new Contact(contact['NAME'], contact['MOBILE']));
        }
      }
    } on PlatformException catch (e) {
      print('Exception ' + e.toString());
    }
    return new Future.value(contacts);
  }
}
