import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { WhipperRoutingModule } from './whipper-routing.module';
import { WelcomeComponent } from './welcome.component';
import { JobsComponent } from './jobs.component';
import { JobDetailComponent } from './job-detail.component';
import { ResultsComponent } from './results.component';
import { ResultDetailComponent } from './result-detail.component';
import { WhipperRestService } from './whipper-rest.service';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    JobsComponent,
    JobDetailComponent,
    ResultsComponent,
    ResultDetailComponent
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
