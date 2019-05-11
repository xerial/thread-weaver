package com.github.j5ik2o.threadWeaver.adaptor.http.presenter

import com.github.j5ik2o.threadWeaver.adaptor.http.json.AddAdministratorIdsResponseJson
import com.github.j5ik2o.threadWeaver.useCase.ThreadWeaverProtocol.AddAdministratorIdsResponse

trait AddAdministratorIdsPresenter extends Presenter[AddAdministratorIdsResponse, AddAdministratorIdsResponseJson]