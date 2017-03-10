import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Job } from './job';

@Injectable()
export class WhipperRestService {
  private basicUrl = 'http://localhost:9080/whipper-rest/';
  private headers = new Headers({'Accept': 'application/json'});
  private jobs: Job[] = null;

  constructor(private http: Http) { }

  getJobs(): Promise<Job[]>{
    return this.http.get(this.basicUrl + 'jobs')
      .toPromise()
      .then(resp => resp.json().data as Job[]);
  }

  private handleError(error:any, toReturn:any): any{
    console.log('ERROR', error);
    return toReturn;
  }
}
