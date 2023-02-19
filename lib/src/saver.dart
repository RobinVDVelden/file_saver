import 'dart:developer';
import 'dart:io';

import 'package:file_saver/src/models/file.model.dart';
import 'package:file_saver/src/utils/helpers.dart';
import 'package:flutter/services.dart';

class Saver {
  final FileModel fileModel;
  Saver({required this.fileModel});

  final MethodChannel _channel = const MethodChannel('file_saver');
  final String _saveAs = "saveAs";
  final String _saveFile = "saveFile";
  final String _somethingWentWrong =
      "Something went wrong, please report the issue https://www.github.com/incrediblezayed/file_saver/issues";
  late String directory = _somethingWentWrong;
  final String _issueLink =
      "https://www.github.com/incrediblezayed/file_saver/issues";

  ///Open File Manager
  Future<String?> saveAs() async {
    String? path;
    if (Platform.isAndroid || Platform.isIOS || Platform.isMacOS) {
      path = await _channel.invokeMethod<String>(_saveAs, fileModel.toMap());
    } else {
      throw UnimplementedError("Unimplemented Error");
    }
    return path;
  }

  /// It takes a file model, converts it to a json string, and sends it to the native code
  ///
  /// Args:
  ///   file (FileModel): The file to be downloaded.
  ///
  /// Returns:
  ///   The directory where the file was saved.
  Future<String> saveFileForWeb() async {
    try {
      bool? downloaded =
          await _channel.invokeMethod<bool>(_saveFile, fileModel.toJson());
      if (downloaded!) {
        directory = "Downloads";
      }
    } catch (e) {
      log("Error: $e");
    }
    return directory;
  }

  Future<String> saveFileForAndroid() async {
    try {
      directory =
          await _channel.invokeMethod<String>(_saveFile, fileModel.toMap()) ??
              "";
    } catch (e) {
      log("Error: $e");
    }
    return directory;
  }

  Future<String> saveFileForOtherPlatforms() async {
    String path = "";
    path = await Helpers.getDirectory() ?? "";
    if (path == "") {
      log("The path was found null or empty, please report the issue at $_issueLink");
    } else {
      String filePath = '$path/${fileModel.name}${fileModel.ext}';
      final File file = File(filePath);
      await file.writeAsBytes(fileModel.bytes);
      bool exist = await file.exists();
      if (exist) {
        directory = file.path;
      } else {
        log("File was not created");
      }
    }
    return directory;
  }
}
