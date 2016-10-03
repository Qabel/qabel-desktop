@sync
Feature: edit sync
    As a user
    in order to adjust a sync to changing needs
    it should be possible to change (the config of) an existing sync

    Scenario: change local folder
        Given a sync A from 'localFolder' to '/remoteFolder'
        And a synced file 'testfile'
        When I change the local folder to 'localFolder2'
        Then 'testfile' exists in local 'localFolder'
        And 'testfile' exists in local 'localFolder2'
        And 'testfile' exists in remote '/remoteFolder'

    Scenario: delete file after changing the sync
        Given a sync C from 'localFolder1' to '/folder1'
        And a synced file 'testfile'
        When I change the local folder to 'localFolder2'
        And I delete the local 'localFolder2/testfile'
        Then no 'testfile' exists in remote '/folder1'
        And 'testfile' exists in local 'localFolder1'
