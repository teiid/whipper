import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';

import 'rxjs/add/operator/share';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/observable/of';

import { Job } from './job';

@Injectable()
export class WhipperRestService {
  private basicUrl = 'http://localhost:9080/whipper-rest/';
  private headers = new Headers({'Accept': 'application/json'});
  private jobs: Job[] = null;

  constructor(private http: Http) { }

  getJobs(refresh:boolean): Observable<Job[]>{
    let obs:Observable<any> = this.http.get(this.basicUrl + 'jobs');
    if(!refresh && this.jobs !== null){
        obs = obs.share();
    }
    obs.map(resp => resp.json().data as Job[])
    obs.catch(err => this.handleError(err, Observable.of<Job[]>([])));
    obs.subscribe();
    return obs;
  }

  private handleError(error:any, toReturn:any): any{
    console.log('ERROR', error);
    return toReturn;
  }
}
