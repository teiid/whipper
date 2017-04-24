import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { WhipperRoutingModule } from './whipper-routing.module';
import { WelcomeComponent } from './welcome.component';
import { JobsComponent } from './jobs.component';
import { JobDetailComponent } from './job-detail.component';
import { ResultDetailComponent } from './result-detail.component';
import { WhipperRestService } from './whipper-rest.service';
import { StartJobComponent } from './start-job.component';
import { QuerySetsComponent } from './query-sets.component';
import { QuerySetDetailComponent } from './query-set-detail.component';
import { SuiteDetailComponent } from './suite-detail.component';
import { ExpectedResultDetailComponent } from './expected-result-detail.component';
import { ScenariosComponent } from './scenarios.component';
import { ScenarioDetailComponent } from './scenario-detail.component';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    JobsComponent,
    JobDetailComponent,
    ResultDetailComponent,
    StartJobComponent,
    QuerySetsComponent,
    QuerySetDetailComponent,
    SuiteDetailComponent,
    ExpectedResultDetailComponent,
    ScenariosComponent,
    ScenarioDetailComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    WhipperRoutingModule,
  ],
  providers: [WhipperRestService],
  bootstrap: [AppComponent]
})
export class AppModule { }
