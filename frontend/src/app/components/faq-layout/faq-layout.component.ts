import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { FaqComponent } from "../faq/faq.component";
import { ApolloTestingModule } from 'apollo-angular/testing';
import { provideRouter } from '@angular/router';

@Component({
  selector: 'app-faq-layout',
  imports: [HeaderComponent, FooterComponent, FaqComponent, ApolloTestingModule],
  templateUrl: './faq-layout.component.html',
  styleUrl: './faq-layout.component.css'
})
export class FaqLayoutComponent {

}
