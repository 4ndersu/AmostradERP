package erp;

public class Supervisor extends Funcionario {
    public Supervisor(String id, String nome, double salario, String email, String senha, String lojaId) {
        super(id, nome, "Supervisor", salario, email, senha, lojaId); // Chama o construtor da classe pai
    }

    @Override
    public boolean podeCadastrarProduto() {
        return false;
    }

    @Override
    public boolean podeCadastrarDepartamento() {
        return false;
    }

    @Override
    public boolean podeCadastrarCliente() {
        return true;
    }

    @Override
    public boolean podeVenderProduto() {
        return true;
    }

    @Override
    public boolean podeVerRelatorios() {
        return true;
    }
}
