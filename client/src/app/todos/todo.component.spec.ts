import { ComponentFixture, TestBed, async } from "@angular/core/testing";
import { Todo } from "./todo";
import { TodoComponent } from "./todo.component";
import { TodoListService } from "./todo-list.service";
import { Observable } from "rxjs";
import { PipeModule } from "../../pipe.module";

describe("Todo component", () => {

    let todoComponent: TodoComponent;
    let fixture: ComponentFixture<TodoComponent>;

    let todoListServiceStub: {
        getTodoById: (todoId: string) => Observable<Todo>
    };

    beforeEach(() => {
        // stub TodoService for test purposes
        todoListServiceStub = {
            getTodoById: (todoId: string) => Observable.of([
                {
                    id: "chris_id",
                    owner: "Chris",
                    status: true,
                    body: "UMM",
                    category: "chris@this.that"
                },
                {
                    id: "pat_id",
                    owner: "Pat",
                    status: true,
                    body: "IBM",
                    category: "pat@something.com"
                },
                {
                    id: "jamie_id",
                    owner: "Jamie",
                    status: false,
                    body: "Frogs, Inc.",
                    category: "jamie@frogs.com"
                }
            ].find(todo => todo.id === todoId))
        };

        TestBed.configureTestingModule({
            imports: [PipeModule],
            declarations: [ TodoComponent ],
            providers:    [ { provide: TodoListService, useValue: todoListServiceStub } ]
        })
    });

    beforeEach(async(() => {
        TestBed.compileComponents().then(() => {
            fixture = TestBed.createComponent(TodoComponent);
            todoComponent = fixture.componentInstance;
        });
    }));

    it("can retrieve Pat by ID", () => {
        todoComponent.setId("pat_id");
        expect(todoComponent.todo).toBeDefined();
        expect(todoComponent.todo.owner).toBe("Pat");
        expect(todoComponent.todo.category).toBe("pat@something.com");
    });

    it("returns undefined for Santa", () => {
        todoComponent.setId("Santa");
        expect(todoComponent.todo).not.toBeDefined();
    });

});
