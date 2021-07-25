package commnet.model.exceptions;

public class InvalidBeanException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private Class<?> clazz;

	public InvalidBeanException(Class<?> clazz, String description, Throwable e){
		super(description, e);
		this.clazz = clazz;
	}
	
	@Override
	public String getMessage(){
		return "Invalid Bean ("+clazz.getName()+ "):" +super.getMessage();
	}
}
