import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'contact.dart';
import 'home_contract.dart';
import 'home_model.dart';
import 'home_presenter.dart';

class HomePage extends StatefulWidget {
  HomePage({Key key}) : super(key: key);

  @override
  _HomePageState createState() => new _HomePageState();
}

class _HomePageState extends State<HomePage> implements View {
  List<Contact> _contacts;

  bool _loadingInProgress;

  HomePresenter _presenter;

  BuildContext _scaffoldContext;

  @override
  void initState() {
    super.initState();
    _loadingInProgress = true;
    _presenter = new HomePresenter(new HomeModel(), this);
    _presenter.viewDisplayed();
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: new AppBar(
        title: new Text("Runtime Permission example"),
      ),
      body: new Builder(
        builder: (BuildContext context) {
          _scaffoldContext = context;
          return _buildBody();
        },
      ),
    );
  }

  Widget _buildBody() {
    if (_loadingInProgress) {
      return new Center(
        child: new CircularProgressIndicator(),
      );
    } else {
      return new ListView(
        scrollDirection: Axis.vertical,
        shrinkWrap: true,
        children: _contacts.map((Contact contact) {
          return _buildListRow(contact);
        }).toList(),
      );
    }
  }

  Widget _buildListRow(Contact contact) {
    return new ListTile(
      title: new Text(contact.displayName),
      subtitle: new Text(contact.mobileNumber),
    );
  }

  @override
  void showErrorMessage() {
    setState(() {
      _loadingInProgress = false;
      _contacts = new List<Contact>();
    });
    Scaffold.of(_scaffoldContext).showSnackBar(new SnackBar(
          content: new Text('No permission'),
          duration: new Duration(seconds: 5),
        ));
  }

  @override
  void showLoadingContactsInProgress() {
    setState(() {
      _loadingInProgress = true;
    });
  }

  @override
  void showContactsWithPhoneNumber(List<Contact> contacts) {
    setState(() {
      _loadingInProgress = false;
      _contacts = contacts;
    });
  }

  @override
  Future showPermissionRationale() {
    return showDialog<Null>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) => new AlertDialog(
            title: new Text('Contacts Permission'),
            content: new SingleChildScrollView(
              child: new ListBody(
                children: <Widget>[
                  new Text('We need this permission because ...'),
                ],
              ),
            ),
            actions: <Widget>[
              new FlatButton(
                child: new Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                  _presenter.viewDisplayed();
                },
              ),
            ],
          ),
    );
  }
}
