import 'dart:async';

import 'contact.dart';
import 'home_contract.dart';

class HomePresenter implements Presenter {
  Model _model;

  View _view;

  HomePresenter(this._model, this._view);

  @override
  Future viewDisplayed() async {
    _view.showLoadingContactsInProgress();
    PermissionState permissionState = await _model.canGetContacts();
    switch (permissionState) {
      case PermissionState.GRANTED:
        List<Contact> contacts =
            await _model.getContactsWithMobilePhoneNumber();
        _view.showContactsWithPhoneNumber(contacts);
        break;
      case PermissionState.DENIED:
        await new Future.delayed(new Duration(seconds: 1));
        _view.showErrorMessage();
        break;
      case PermissionState.SHOW_RATIONALE:
        _view.showPermissionRationale();
        break;
    }
    return null;
  }
}
